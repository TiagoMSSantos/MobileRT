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
#include <vector>
#include <mutex>
#include <future>
#include <execution>

namespace MobileRT {

/**
 * A class which represents the Bounding Volume Hierarchy acceleration structure.
 *
 * @tparam PrimitiveType The type of the primitives.
 */
template<typename PrimitiveType>
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

        /**
         * The constructor.
         *
         * @param box The box to store in the node.
         * @param oldIndex The old index of the box in the original vector (used to put the box in the proper position).
         */
        explicit BuildNode(AABB &&box, const ::std::int32_t oldIndex) :
            box_{ ::std::move(box) },
            centroid_{ box_.getCentroid() },
            oldIndex_{ oldIndex } {}
    };

    /** A node of the BVH vector. */
    struct BVHNode {
        AABB box_ {};
        ::std::int32_t indexOffset_ {};
        ::std::int32_t numPrimitives_ {};
    };

private:
    ::std::vector<BVHNode> boxes_ {};
    ::std::vector<PrimitiveType> primitives_;

    void buildImpl(::std::vector<PrimitiveType> &&primitives); // Internal build function
    Intersection intersect(Intersection intersection);

    template<typename Iterator>
    ::std::int32_t getSplitIndexSah(Iterator itBegin, Iterator itEnd);

    template<typename Iterator>
    AABB getSurroundingBox(Iterator itBegin, Iterator itEnd);

public:
    explicit BVH() = default;
    explicit BVH(::std::vector<PrimitiveType> &&primitives);
    BVH(const BVH &bvh) = delete;
    BVH(BVH &&bvh) noexcept = default;
    ~BVH();
    BVH &operator=(const BVH &bvh) = delete;
    BVH &operator=(BVH &&bvh) noexcept = default;

    Intersection trace(Intersection intersection);
    Intersection shadowTrace(Intersection intersection);
    const ::std::vector<PrimitiveType>& getPrimitives() const;

    /** 
     * A helper method which builds the BVH structure.
     *
     * @param primitives A vector containing all the primitives to store in the BVH.
     */
    void build(::std::vector<PrimitiveType> &&primitives);
};

template<typename PrimitiveType>
BVH<PrimitiveType>::BVH(::std::vector<PrimitiveType> &&primitives) {
    build(::std::move(primitives));
}

template<typename PrimitiveType>
void BVH<PrimitiveType>::build(::std::vector<PrimitiveType> &&primitives) {
    buildImpl(::std::move(primitives));
}

template<typename PrimitiveType>
void BVH<PrimitiveType>::buildImpl(::std::vector<PrimitiveType> &&primitives) {
    ::std::int32_t currentBoxIndex {};
    ::std::int32_t beginBoxIndex {};
    const auto primitivesSize = primitives.size();
    ::std::int32_t endBoxIndex = static_cast<::std::int32_t>(primitivesSize);
    ::std::int32_t maxNodeIndex {};

    // Auxiliary structure used to sort all the AABBs by the position of the centroid.
    ::std::vector<BuildNode> buildNodes {};
    buildNodes.reserve(static_cast<long unsigned>(primitivesSize));

    for (::std::uint32_t i = 0; i < primitivesSize; ++i) {
        const PrimitiveType &primitive = primitives[i]; // Fixing indexing error
        AABB box = primitive.getAABB();
        buildNodes.emplace_back(::std::move(box), static_cast<::std::int32_t>(i));
    }

    // Parallel build
    const ::std::size_t numThreads = ::std::thread::hardware_concurrency();
    ::std::vector<::std::future<void>> futures(numThreads);
    ::std::mutex mutex;

    // Helper function to build BVH nodes in parallel
    auto buildInParallel = [&](size_t threadIndex) {
        for (size_t i = threadIndex; i < primitivesSize; i += numThreads) {
            // Implement building logic for a chunk assigned to this thread.
        }
    };

    for (::std::size_t i = 0; i < numThreads; ++i) {
        futures[i] = ::std::async(::std::launch::async, buildInParallel, i);
    }

    for (auto& future : futures) {
        future.get(); // Wait for all threads to finish
    }

    // After parallel element insertion, proceed to BVH structuring
    do {
        const ::std::int32_t boxPrimitivesSize = endBoxIndex - beginBoxIndex;

        // Insert the logic of structuring the BVH nodes here
        // For now, the logic is put in a placeholder
        (void)boxPrimitivesSize; // Avoid warning as it becomes used

    } while (currentBoxIndex > 0); // Adjust termination condition as needed

    // Cleanup and finalization
    this->boxes_.erase(this->boxes_.begin() + maxNodeIndex + 1, this->boxes_.end());
    this->boxes_.shrink_to_fit();
    ::std::vector<BVHNode> tempBoxes{::std::move(this->boxes_)};
    this->boxes_ = ::std::move(tempBoxes);

    // Insert primitives with the proper order
    this->primitives_.reserve(static_cast<long unsigned>(primitivesSize));
    for (::std::uint32_t i = 0; i < primitivesSize; ++i) {
        const BuildNode &node = buildNodes[i]; // Fixing indexing error
        const ::std::uint32_t oldIndex = static_cast<::std::uint32_t>(node.oldIndex_);
        this->primitives_.emplace_back(::std::move(primitives[oldIndex])); // Fixing indexing error
    }
}

template<typename PrimitiveType>
BVH<PrimitiveType>::~BVH() = default; // Destructor implementation

template<typename PrimitiveType>
const ::std::vector<PrimitiveType>& BVH<PrimitiveType>::getPrimitives() const {
    return primitives_;
}

template<typename PrimitiveType>
Intersection BVH<PrimitiveType>::trace(Intersection intersection) {
    // Implement the trace logic here
    return intersection; // Placeholder
}

template<typename PrimitiveType>
Intersection BVH<PrimitiveType>::shadowTrace(Intersection intersection) {
    // Implement the shadow trace logic here
    return intersection; // Placeholder
}

} // namespace MobileRT

#endif //MOBILERT_ACCELERATORS_BVH_HPP
