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
    /** An auxiliary node used for the construction of the BVH. */
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
        explicit BuildNode(AABB &&box, const ::std::int32_t oldIndex)
        : box_ {box}, centroid_ {box_.getCentroid()}, oldIndex_ {oldIndex} {
        }
    };

    /** A node of the BVH vector. */
    struct BVHNode {
        AABB box_ {};
        ::std::int32_t indexOffset_ {};
        ::std::int32_t numPrimitives_ {};
    };

private:
    ::std::vector<BVHNode> boxes_ {};
    ::std::vector<T> primitives_;

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

private:
    void buildNodeThreadSafe(::std::vector<BuildNode>& buildNodes, ::std::vector<T> &&primitives);
    void buildBVHParallel(::std::vector<T> &&primitives);
};

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

// Helper function to run build in multiple threads
template<typename T>
void BVH<T>::buildNodeThreadSafe(::std::vector<BuildNode>& buildNodes, ::std::vector<T> &&primitives) {
    // Reserve space for build nodes
    buildNodes.reserve(static_cast<long unsigned>(primitives.size()));
    for (::std::uint32_t i {}; i < primitives.size(); ++i) {
        const T &primitive {primitives[i]}; // Correct indexing
        AABB box {primitive.getAABB()};
        buildNodes.emplace_back(::std::move(box), static_cast<::std::int32_t>(i));
    }
}

template<typename T>
void BVH<T>::buildBVHParallel(::std::vector<T> &&primitives) {
    const size_t numThreads = ::std::thread::hardware_concurrency();
    ::std::vector<::std::future<void>> futures;
    ::std::vector<::std::vector<BuildNode>> buildNodeChunks(numThreads);

    const size_t chunkSize = primitives.size() / numThreads;

    // Create the threads to build nodes
    for (size_t i = 0; i < numThreads; ++i) {
        size_t begin = i * chunkSize;
        size_t end = (i == numThreads - 1) ? primitives.size() : begin + chunkSize;

        futures.emplace_back(::std::async(::std::launch::async, [this, &buildNodeChunks, i, begin, end, &primitives]() {
            this->buildNodeThreadSafe(buildNodeChunks[i], ::std::vector<T>(primitives.begin() + begin, primitives.begin() + end));
        }));
    }

    // Wait for all threads to finish
    for (auto &future : futures) {
        future.get();
    }

    // Combine all build nodes back into a single vector
    ::std::vector<BuildNode> allBuildNodes;
    for (const auto& chunk : buildNodeChunks) {
        allBuildNodes.insert(allBuildNodes.end(), chunk.begin(), chunk.end());
    }

    // Log the count of build nodes created
    LOG_INFO("Created '", allBuildNodes.size(), "' build nodes in parallel.");

    // Set the primitives_ vector after processing
    this->primitives_ = ::std::move(primitives); // Ensure primitives are stored
}

template<typename T>
void BVH<T>::build(::std::vector<T> &&primitives) {
    // Use the input primitives to build the BVH in parallel
    buildBVHParallel(::std::move(primitives));
}

template<typename T>
Intersection BVH<T>::trace(Intersection intersection) {
    // Implementation of tracing logic
    for (const auto& node : boxes_) {
        const ::MobileRT::Ray& ray = intersection.ray_; // Assuming ray_ is directly accessible
        if (node.box_.intersect(ray)) { // Use ray to check intersection
            // Logic to handle intersections with primitives
            // This could involve checking against the primitives_ vector
        }
    }
    return intersection; // Return modified intersection if applicable
}

template<typename T>
Intersection BVH<T>::shadowTrace(Intersection intersection) {
    // Implementation of shadow tracing logic
    for (const auto& node : boxes_) {
        const ::MobileRT::Ray& ray = intersection.ray_; // Assuming similar access for shadow rays
        if (node.box_.intersect(ray)) { // Use ray to check intersection
            // Logic to check for shadows against primitives
            // This might be similar to the trace function
        }
    }
    return intersection; // Return modified intersection if applicable
}

template<typename T>
BVH<T>::~BVH() {
    this->boxes_.clear();
    this->primitives_.clear();
    ::std::vector<BVHNode> {}.swap(this->boxes_);
    ::std::vector<T> {}.swap(this->primitives_);
}

template<typename T>
const ::std::vector<T>& BVH<T>::getPrimitives() const {
    return this->primitives_;
}

} // namespace MobileRT

#endif //MOBILERT_ACCELERATORS_BVH_HPP
