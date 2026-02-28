#include <future>
#include <thread>
#include <mutex>
#include <vector>
#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Scene.hpp"
#include "MobileRT/Utils/Utils.hpp"
#include <algorithm>
#include <array>
#include <boost/sort/spreadsort/spreadsort.hpp>
#include <glm/glm.hpp>
#include <random>

namespace MobileRT {
    
// Other parts of the BVH remain unchanged...

private:
std::mutex mutex_; // mutex to protect shared resources

void buildParallel(::std::vector<T> &&primitives, int numThreads);

// The modified build function
template<typename T>
void BVH<T>::build(::std::vector<T> &&primitives) {
    std::int32_t currentBoxIndex {};
    std::int32_t beginBoxIndex {};
    const long long unsigned primitivesSize {primitives.size()};
    std::int32_t endBoxIndex {static_cast<std::int32_t> (primitivesSize)};
    std::int32_t maxNodeIndex {};
    
    std::vector<BuildNode> buildNodes {};
    buildNodes.reserve(static_cast<long unsigned> (primitivesSize));
    for (std::uint32_t i {}; i < primitivesSize; ++i) {
        const T &primitive {primitives[i]};
        AABB &&box {primitive.getAABB()};
        buildNodes.emplace_back(std::move(box), static_cast<std::int32_t> (i));
    }
    
    // Convert to parallel build
    const int numThreads = std::thread::hardware_concurrency();
    buildParallel(std::move(buildNodes), numThreads);
}

template<typename T>
void BVH<T>::buildParallel(std::vector<BuildNode> &&buildNodes, int numThreads) {
    std::vector<std::future<void>> futures(numThreads);
    size_t chunkSize = buildNodes.size() / numThreads;

    for (int threadIndex = 0; threadIndex < numThreads; ++threadIndex) {
        futures[threadIndex] = std::async([this, &buildNodes, threadIndex, chunkSize]() {
            size_t start = threadIndex * chunkSize;
            size_t end = (threadIndex == numThreads - 1) ? buildNodes.size() : start + chunkSize;
            
            for (size_t i = start; i < end; ++i) {
                // Perform operations on the build nodes as needed
                // This is where threading interaction should happen

                // Dummy operation to simulate usage
                std::lock_guard<std::mutex> lock(mutex_);
                // Example of using buildNodes safely
                // Optional: Modify the shared structure, if needed
            }
        });
    }
    
    // Wait for all threads to finish
    for (auto &future : futures) {
        future.get();
    }

    // Continue with merging results and building the BVH structure
}

// Continue with the rest of the BVH implementation...

}
