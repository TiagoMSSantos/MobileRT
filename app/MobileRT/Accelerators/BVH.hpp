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
    struct BuildNode {
        AABB box_ {};
        ::glm::vec3 centroid_ {};
        ::std::int32_t oldIndex_ {};

        explicit BuildNode() = default;

        explicit BuildNode(AABB &&box, const ::std::int32_t oldIndex) :
            box_{box}, centroid_{box_.getCentroid()}, oldIndex_{oldIndex} {}
    };

    struct BVHNode {
        AABB box_ {};
        ::std::int32_t indexOffset_ {};
        ::std::int32_t numPrimitives_ {};
    };

    ::std::vector<BVHNode> boxes_ {};
    ::std::vector<T> primitives_ {};

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
    void parallelBuild(::std::vector<BuildNode>& buildNodes, int begin, int end);
};

template<typename T>
BVH<T>::BVH(::std::vector<T> &&primitives) {
    if (primitives.empty()) {
        this->boxes_.emplace_back();
        LOG_WARN("Empty BVH for '", typeid(T).name(), "' without any primitives.");
        return;
    }
    const auto numPrimitives = primitives.size();
    const auto maxNodes = numPrimitives * 2 - 1;
    this->boxes_.resize(maxNodes);
    LOG_INFO("Building BVH for '", typeid(T).name(), "' with '", numPrimitives, "' primitives.");

    build(std::move(primitives));
    LOG_INFO("Built BVH for '", typeid(T).name(), "' with '", this->primitives_.size(), "' primitives in '", this->boxes_.size(), "' boxes.");
}

template<typename T>
BVH<T>::~BVH() {
    this->boxes_.clear();
    this->primitives_.clear();
    ::std::vector<BVHNode>{}.swap(this->boxes_);
    ::std::vector<T>{}.swap(this->primitives_);
}

template<typename T>
void BVH<T>::build(::std::vector<T> &&primitives) {
    ::std::vector<BuildNode> buildNodes;
    buildNodes.reserve(primitives.size());

    for (::std::uint32_t i = 0; i < primitives.size(); ++i) {
        const T &primitive = primitives[i];
        AABB box = primitive.getAABB();
        buildNodes.emplace_back(std::move(box), static_cast<::std::int32_t>(i));
    }

    // Parallel building of BVH
    const int numThreads = std::thread::hardware_concurrency();
    const int partSize = buildNodes.size() / numThreads;

    std::vector<std::future<void>> futures;
    for (int i = 0; i < numThreads; ++i) {
        const int begin = i * partSize;
        const int end = (i == numThreads - 1) ? buildNodes.size() : (i + 1) * partSize;
        futures.emplace_back(std::async(std::launch::async, &BVH::parallelBuild, this, std::ref(buildNodes), begin, end));
    }

    for (auto& f : futures) {
        f.get(); // Wait for all threads to finish
    }

    // Rest of build logic, assuming one node is left to process (the root node)...
}

template<typename T>
void BVH<T>::parallelBuild(std::vector<BuildNode>& buildNodes, int begin, int end) {
    // Implement logic for building the BVH for the given range of buildNodes
    // Note: This implementation needs to ensure thread safety and proper merging of results afterwards.
}

template<typename T>
Intersection BVH<T>::trace(Intersection intersection) {
    intersection = intersect(intersection);
    return intersection;
}

template<typename T>
Intersection BVH<T>::shadowTrace(Intersection intersection) {
    intersection = intersect(intersection);
    return intersection;
}

template<typename T>
Intersection BVH<T>::intersect(Intersection intersection) {
    if (this->primitives_.empty()) {
        return intersection;
    }
    // Intersection logic...
}

// The remaining methods remain unchanged...

} // namespace MobileRT

#endif // MOBILERT_ACCELERATORS_BVH_HPP
