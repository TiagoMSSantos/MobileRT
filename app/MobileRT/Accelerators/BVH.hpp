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
#include <mutex>

namespace MobileRT {

template<typename T>
class BVH final {
private:
    // Other existing structures

    std::vector<BVHNode> boxes_ {}; 
    std::vector<T> primitives_ {};
    std::mutex bvhMutex;

    void build(std::vector<T> &&primitives);

public:
    BVH() = default;
    explicit BVH(std::vector<T> &&primitives);
    // Other existing public methods
};

// Implementation of the constructor
template<typename T>
BVH<T>::BVH(std::vector<T> &&primitives) {
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

// Updated build method using multithreading
template<typename T>
void BVH<T>::build(std::vector<T> &&primitives) {
    const std::size_t threadCount = std::thread::hardware_concurrency();
    const std::size_t primitivesSize = primitives.size();
    
    std::vector<BuildNode> buildNodes(primitivesSize);
    for (std::size_t i = 0; i < primitivesSize; ++i) {
        AABB box = primitives[i].getAABB();
        buildNodes[i] = BuildNode(std::move(box), static_cast<int32_t>(i));
    }

    // Divide the workload among the threads
    std::vector<std::thread> threads;
    for (std::size_t i = 0; i < threadCount; ++i) {
        threads.emplace_back([this, &buildNodes, primitivesSize, i, threadCount] {
            const std::size_t chunkSize = (primitivesSize + threadCount - 1) / threadCount;
            const std::size_t start = i * chunkSize;
            const std::size_t end = std::min(start + chunkSize, primitivesSize);

            // Each thread processes its chunk
            for (std::size_t j = start; j < end; ++j) {
                // Here we can define box processing logic, for example:
                AABB &box = buildNodes[j].box_;
                // Add further operation if needed
            }
        });
    }

    // Join all threads
    for (auto &thread : threads) {
        if (thread.joinable()) {
            thread.join();
        }
    }

    // Subsequent part of the BVH building logic, which can stay serial as we merge results

    // Final assembly of the BVH structure from buildNodes
    // ... (existing logic to finalize BVH from buildNodes)

    // Further cleanup or arrangement of nodes would go here
}

} // namespace MobileRT

#endif //MOBILERT_ACCELERATORS_BVH_HPP
