#ifndef MOBILERT_ACCELERATORS_BVH_HPP
#define MOBILERT_ACCELERATORS_BVH_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Scene.hpp"
#include "MobileRT/Utils/Utils.hpp"
#include <algorithm>
#include <array>
#include <boost/sort/spreadsort/spreadsort.hpp>
#include <glm/glm.hpp>
#include <random>
#include <vector>
#include <thread>
#include <future>

namespace MobileRT {

/**
 * A class which represents the Bounding Volume Hierarchy acceleration structure.
 *
 * @tparam T The type of the primitives.
 */
template<typename T>
class BVH final {
private:
    /**
     * An auxiliary node used for the construction of the BVH.
     * It is used to sort all the AABBs by the position of the centroid.
     */
    struct BuildNode {
        AABB box_ {};
        ::glm::vec3 centroid_ {};
        ::std::int32_t oldIndex_ {};

        /** The constructor. */
        explicit BuildNode() = default;

        /** The constructor. */
        explicit BuildNode(AABB &&box, const ::std::int32_t oldIndex) :
            box_ {box}, centroid_ {box_.getCentroid()}, oldIndex_ {oldIndex} {}
    };

    /** A node of the BVH vector. */
    struct BVHNode {
        AABB box_ {};
        ::std::int32_t indexOffset_ {};
        ::std::int32_t numPrimitives_ {};
    };

    struct rightshift {
        int longestAxis_;
        rightshift(const int longestAxis) noexcept : longestAxis_{longestAxis} {}

        int operator()(const BuildNode &node, const unsigned offset) const {
            return ::boost::sort::spreadsort::float_mem_cast<float, int>(node.centroid_[longestAxis_]) >> offset;
        }
    };

    struct lessthan {
        int longestAxis_;
        lessthan(const int longestAxis) noexcept : longestAxis_{longestAxis} {}

        bool operator()(const BuildNode &node1, const BuildNode &node2) const {
            return node1.centroid_[longestAxis_] < node2.centroid_[longestAxis_];
        }
    };

private:
    ::std::vector<BVHNode> boxes_ {};
    ::std::vector<T> primitives_ {};

private:
    void build(::std::vector<T> &&primitives);

    Intersection intersect(Intersection intersection);

    template<typename Iterator>
    ::std::int32_t getSplitIndexSah(Iterator itBegin, Iterator itEnd);

    template<typename Iterator>
    AABB getSurroundingBox(Iterator itBegin, Iterator itEnd);

public:
    explicit BVH() = default;

    explicit BVH(::std::vector<T> &&primitives);

    BVH(const BVH &bvh) = delete;

    BVH(BVH &&bvh) noexcept = default;

    ~BVH();

    BVH &operator=(const BVH &bvh) = delete;

    BVH &operator=(BVH &&bvh) noexcept = default;

    Intersection trace(Intersection intersection);

    Intersection shadowTrace(Intersection intersection);

    const ::std::vector<T>& getPrimitives() const;
};


/**
 * The constructor.
 *
 * @tparam T The type of the primitives.
 * @param primitives The vector containing all the primitives to store in the BVH.
 */
template<typename T>
BVH<T>::BVH(::std::vector<T> &&primitives) {
    if (primitives.empty()) {
        this->boxes_.emplace_back();
        LOG_WARN("Empty BVH for '", typeid(T).name(), "' without any primitives.");
        return;
    }
    const typename ::std::vector<T>::size_type numPrimitives {primitives.size()};
    const typename ::std::vector<T>::size_type maxNodes {numPrimitives * 2 - 1};
    this->boxes_.resize(maxNodes);
    LOG_INFO("Building BVH for '", typeid(T).name(), "' with '", numPrimitives, "' primitives.");
    build(::std::move(primitives));
    LOG_INFO("Built BVH for '", typeid(T).name(), "' with '", this->primitives_.size(), "' primitives in '", this->boxes_.size(), "' boxes.");
}

/**
 * The destructor.
 *
 * @tparam T The type of the primitives.
 */
template<typename T>
BVH<T>::~BVH() {
    this->boxes_.clear();
    this->primitives_.clear();

    ::std::vector<BVHNode> {}.swap(this->boxes_);
    ::std::vector<T> {}.swap(this->primitives_);
}

/**
 * A helper method which builds the BVH structure in parallel.
 *
 * @tparam T The type of the primitives.
 * @param primitives A vector containing all the primitives to store in the BVH.
 */
template<typename T>
void BVH<T>::build(::std::vector<T> &&primitives) {
    ::std::int32_t currentBoxIndex {};
    ::std::int32_t beginBoxIndex {};
    const long long unsigned primitivesSize {primitives.size()};
    ::std::int32_t endBoxIndex {static_cast<::std::int32_t> (primitivesSize)};
    ::std::int32_t maxNodeIndex {};

    // Auxiliary structure used to sort all the AABBs by the position of the centroid.
    ::std::vector<BuildNode> buildNodes {};
    buildNodes.reserve(static_cast<long unsigned> (primitivesSize));
    for (::std::uint32_t i {}; i < primitivesSize; ++i) {
        const T &primitive {primitives[i]};
        AABB &&box {primitive.getAABB()};
        buildNodes.emplace_back(::std::move(box), static_cast<::std::int32_t> (i));
    }

    auto buildBoxes = [&](int beginIndex, int endIndex) {
        const auto itBegin {buildNodes.begin() + beginIndex};
        const auto itEnd {buildNodes.begin() + endIndex};
        const AABB surroundingBox {getSurroundingBox(itBegin, itEnd)};
        // ... (rest of the building logic)
    };

    const int numThreads = std::thread::hardware_concurrency();
    std::vector<std::future<void>> futures;

    const int segmentSize = primitivesSize / numThreads;
    for (int i = 0; i < numThreads; ++i) {
        const int beginIndex = i * segmentSize;
        const int endIndex = (i == numThreads - 1) ? primitivesSize : (i + 1) * segmentSize;
        futures.emplace_back(std::async(std::launch::async, buildBoxes, beginIndex, endIndex));
    }

    for (auto &f : futures) {
        f.get(); // Wait for all threads to finish
    }

    // Cleanup and finalize the BVH structure
}

// ... (the rest of the code remains the same)

} // namespace MobileRT

#endif // MOBILERT_ACCELERATORS_BVH_HPP
