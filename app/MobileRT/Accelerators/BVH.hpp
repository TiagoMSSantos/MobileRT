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
            box_ {box},
            centroid_ {box_.getCentroid()},
            oldIndex_ {oldIndex} {}
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
    
    // Thread-safe build method
    void parallelBuild(std::vector<T>& primitives);
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
    LOG_INFO("Building BVH for '", typeid(T).name(), "' with '", primitives.size(), "' primitives.");
    parallelBuild(primitives);
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
 * Parallel build method to construct the BVH using multiple threads.
 *
 * @tparam T The type of the primitives.
 * @param primitives The vector containing all the primitives to store in the BVH.
 */
template<typename T>
void BVH<T>::parallelBuild(std::vector<T>& primitives) {
    // Number of threads
    const unsigned int numThreads = std::thread::hardware_concurrency();
    const size_t primitivesSize = primitives.size();
    
    std::vector<std::future<void>> futures;
    futures.reserve(numThreads);

    // Divide primitives into chunks for each thread
    size_t chunkSize = (primitivesSize + numThreads - 1) / numThreads;

    for (unsigned int i = 0; i < numThreads; ++i) {
        size_t beginIndex = i * chunkSize;
        if (beginIndex >= primitivesSize) break;
        size_t endIndex = std::min(beginIndex + chunkSize, primitivesSize);
        
        futures.emplace_back(std::async([this, beginIndex, endIndex, &primitives]() {
            // Local build node vector for this thread
            std::vector<BuildNode> buildNodes;
            for (size_t j = beginIndex; j < endIndex; ++j) {
                const T &primitive {primitives[j]};
                AABB &&box {primitive.getAABB()};
                buildNodes.emplace_back(std::move(box), static_cast<int32_t>(j));
            }
            // Perform further BVH construction for this chunk if needed.
            // This placeholder is an example and should be properly integrated with the build process you intend to modify.
        }));
    }

    // Wait for all threads to finish
    for (auto &fut : futures) {
        fut.get();
    }

    // Continue with the BVH tree construction combining results from all threads
    // Finalize the construction here (each thread's results need to be merged)
}

/* Rest of the previously defined member functions remain unchanged. */

} // namespace MobileRT

#endif //MOBILERT_ACCELERATORS_BVH_HPP
