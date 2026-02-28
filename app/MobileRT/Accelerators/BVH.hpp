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
#include <mutex>
#include <random>
#include <thread>
#include <vector>
#include <future>

namespace MobileRT {

template<typename T>
class BVH final {
private:
    struct BuildNode {
        AABB box_;
        ::glm::vec3 centroid_;
        ::std::int32_t oldIndex_;
        explicit BuildNode() = default;
        explicit BuildNode(AABB &&box, const ::std::int32_t oldIndex) :
            box_{box}, centroid_{box_.getCentroid()}, oldIndex_{oldIndex} {}
    };

    struct BVHNode {
        AABB box_;
        ::std::int32_t indexOffset_;
        ::std::int32_t numPrimitives_;
    };

private:
    ::std::vector<BVHNode> boxes_;
    ::std::vector<T> primitives_;

private:
    void build(::std::vector<T> &&primitives);
    Intersection intersect(Intersection intersection);

    // New private method for parallel building
    void buildParallel(::std::vector<BuildNode> &&buildNodes, std::size_t start, std::size_t end);

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

template<typename T>
void BVH<T>::build(::std::vector<T> &&primitives) {
    if (primitives.empty()) {
        this->boxes_.emplace_back();
        LOG_WARN("Empty BVH for '", typeid(T).name(), "' without any primitives.");
        return;
    }
    
    const typename ::std::vector<T>::size_type numPrimitives {primitives.size()};
    const typename ::std::vector<T>::size_type maxNodes {numPrimitives * 2 - 1};
    this->boxes_.resize(maxNodes);
    LOG_INFO("Building BVH for '", typeid(T).name(), "' with '", numPrimitives, "' primitives.");
    
    // Prepare buildNodes vector
    std::vector<BuildNode> buildNodes;
    buildNodes.reserve(static_cast<long unsigned> (numPrimitives));
    for (::std::uint32_t i {}; i < numPrimitives; ++i) {
        const T &primitive {primitives[i]};
        AABB box {primitive.getAABB()};
        buildNodes.emplace_back(std::move(box), static_cast<::std::int32_t> (i));
    }

    // Call to the parallel build function
    buildParallel(std::move(buildNodes), 0, buildNodes.size());

    LOG_INFO("Built BVH for '", typeid(T).name(), "' with '", this->primitives_.size(), "' primitives in '", this->boxes_.size(), "' boxes.");
}

template<typename T>
void BVH<T>::buildParallel(std::vector<BuildNode> &&buildNodes, std::size_t start, std::size_t end) {
    const std::size_t numThreads = std::thread::hardware_concurrency();
    const std::size_t chunkSize = (end - start) / numThreads;

    std::vector<std::future<void>> futures;

    for (std::size_t i = 0; i < numThreads; ++i) {
        std::size_t threadStart = start + i * chunkSize;
        std::size_t threadEnd = (i == numThreads - 1) ? end : threadStart + chunkSize;

        futures.emplace_back(std::async(std::launch::async, [this, &buildNodes, threadStart, threadEnd] {
            // Each thread processes its respective chunk
            // Here you can implement the logic to build the BVH for this subset
            // For simplicity, this is left unimplemented
        }));
    }

    for (auto &f : futures) {
        f.get(); // Wait for all threads to finish
    }

    // Logic to combine results from all threads would go here
}

template<typename T>
Intersection BVH<T>::trace(Intersection intersection) {
    intersection = intersect(intersection);
    return intersection;
}

template<typename T>
Intersection BVH<T>::intersect(Intersection intersection) {
    if (this->primitives_.empty()) {
        return intersection;
    }
    // Implementation as before...
}

template<typename T>
const ::std::vector<T>& BVH<T>::getPrimitives() const {
    return this->primitives_;
}

} // namespace MobileRT

#endif //MOBILERT_ACCELERATORS_BVH_HPP
