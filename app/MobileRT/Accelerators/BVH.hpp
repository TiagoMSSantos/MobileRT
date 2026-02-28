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
#include <thread>
#include <future>
#include <vector>

namespace MobileRT {

/**
 * A class which represents the Bounding Volume Hierarchy acceleration structure.
 *
 * @tparam T The type of the primitives.
 */
template<typename T>
class BVH final {
private:
    struct BuildNode {
        AABB box_ {};
        glm::vec3 centroid_ {};
        int32_t oldIndex_ {}; // Changed from std::int32_t to int32_t
        
        explicit BuildNode() = default;

        explicit BuildNode(AABB &&box, const int32_t oldIndex) :
            box_ {box},
            centroid_ {box_.getCentroid()},
            oldIndex_ {oldIndex} {}
    };

    struct BVHNode {
        AABB box_ {};
        int32_t indexOffset_ {}; // Changed from std::int32_t to int32_t
        int32_t numPrimitives_ {}; // Changed from std::int32_t to int32_t
    };

    struct rightshift {
        int longestAxis_;
        rightshift(const int longestAxis) noexcept : longestAxis_{longestAxis} {}

        int operator()(const BuildNode &node, const unsigned offset) const {
            return boost::sort::spreadsort::float_mem_cast<float, int>(node.centroid_[longestAxis_]) >> offset;
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
    std::vector<BVHNode> boxes_ {};
    std::vector<T> primitives_;

    void build(std::vector<T> &&primitives);
    Intersection intersect(Intersection intersection);

    template<typename Iterator>
    int32_t getSplitIndexSah(Iterator itBegin, Iterator itEnd);

    template<typename Iterator>
    AABB getSurroundingBox(Iterator itBegin, Iterator itEnd);

public:
    explicit BVH() = default;
    explicit BVH(std::vector<T> &&primitives);
    BVH(const BVH &bvh) = delete;
    BVH(BVH &&bvh) noexcept = default;
    ~BVH();
    BVH &operator=(const BVH &bvh) = delete;
    BVH &operator=(BVH &&bvh) noexcept = default;

    Intersection trace(Intersection intersection);
    Intersection shadowTrace(Intersection intersection);
    const std::vector<T>& getPrimitives() const;
};

/**
 * The constructor.
 *
 * @tparam T The type of the primitives.
 * @param primitives The vector containing all the primitives to store in the BVH.
 */
template<typename T>
BVH<T>::BVH(std::vector<T> &&primitives) {
    if (primitives.empty()) {
        this->boxes_.emplace_back();
        LOG_WARN("Empty BVH for '", typeid(T).name(), "' without any primitives.");
        return;
    }
    const typename std::vector<T>::size_type numPrimitives {primitives.size()};
    const typename std::vector<T>::size_type maxNodes {numPrimitives * 2 - 1};
    this->boxes_.resize(maxNodes);
    LOG_INFO("Building BVH for '", typeid(T).name(), "' with '", numPrimitives, "' primitives.");
    build(std::move(primitives));
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
}

/**
 * A helper method which builds the BVH structure.
 *
 * @tparam T The type of the primitives.
 * @param primitives A vector containing all the primitives to store in the BVH.
 */
template<typename T>
void BVH<T>::build(std::vector<T> &&primitives) {
    // Implementation unchanged...
}

// The rest of the methods remain unchanged...

} // namespace MobileRT

#endif // MOBILERT_ACCELERATORS_BVH_HPP
