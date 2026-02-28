```cpp
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

template<typename T>
class BVH final {
private:
    // ... [Rest of the existing code remains unchanged] ...

private:
    void build(std::vector<T> &&primitives);
    void buildNode(std::vector<BuildNode>& buildNodes, int beginIndex, int endIndex, int currentBoxIndex);
    Intersection intersect(Intersection intersection);

    // ... [Rest of the existing code remains unchanged] ...

public:
    explicit BVH() = default;

    explicit BVH(std::vector<T> &&primitives);

    // ... [Rest of the existing code remains unchanged] ...

};

// Split the work of building nodes across multiple threads.
template<typename T>
void BVH<T>::buildNode(std::vector<BuildNode>& buildNodes, int beginIndex, int endIndex, int currentBoxIndex) {
    // Your existing code for building nodes here ...
    // For example, this method would contain the existing logic for creating the BVH nodes.
}

template<typename T>
void BVH<T>::build(std::vector<T> &&primitives) {
    std::vector<BuildNode> buildNodes;
    // Populate buildNodes with primitives' AABBs ...
    
    // Example: Populate buildNodes (unchanged)
    for (uint32_t i = 0; i < primitives.size(); ++i) {
        const T& primitive = primitives[i];
        buildNodes.emplace_back(primitive.getAABB(), static_cast<int32_t>(i));
    }
    
    // Create threads to build BVH nodes in parallel
    const int numThreads = std::thread::hardware_concurrency();
    std::vector<std::future<void>> futures;
    
    int sizePerThread = buildNodes.size() / numThreads;
    for (int i = 0; i < numThreads; ++i) {
        int beginIndex = i * sizePerThread;
        int endIndex = (i == numThreads - 1) ? buildNodes.size() : beginIndex + sizePerThread;
        futures.emplace_back(std::async(std::launch::async, &BVH<T>::buildNode, this, std::ref(buildNodes), beginIndex, endIndex, i));
    }
    
    // Wait for all threads to complete
    for (auto& future : futures) {
        future.get();
    }
    
    // Continue with the logic to finalize the BVH structure
    // Existing code that finalizes the BVH ...
}

// ... [Rest of the existing methods remain unchanged] ...

} // namespace MobileRT

#endif // MOBILERT_ACCELERATORS_BVH_HPP
```

This code introduces multi-threading to the `BVH::build` method using asynchronous tasks. The `buildNode` function is designed to handle the task of building BVH nodes in parallel across available threads, leveraging the CPU's capabilities to perform the work concurrently. The number of threads created corresponds to the number of hardware threads supported by the system for optimal performance. 

After implementing these improvements, make sure to test the new functionality extensively to ensure thread-safety and performance benefits.
