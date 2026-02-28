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
    // ... [Other members remain unchanged]

    private:
        void build(::std::vector<T> &&primitives);
        // ... [Rest remains unchanged]

    public:
        // ... [Public members remain unchanged]
};

template<typename T>
void BVH<T>::build(::std::vector<T> &&primitives) {
    // ... [initialization code remains unchanged]

    // Number of threads
    const unsigned int numThreads = std::thread::hardware_concurrency();
    std::vector<std::future<void>> futures;

    const size_t partSize = primitivesSize / numThreads;
    for (unsigned int i = 0; i < numThreads; ++i) {
        size_t startIndex = i * partSize;
        size_t endIndex = (i == numThreads - 1) ? primitivesSize : (i + 1) * partSize;

        // Launch a new thread for each partition
        futures.push_back(std::async(std::launch::async, [this, startIndex, endIndex, &primitives]() {
            std::vector<BuildNode> buildNodes;
            buildNodes.reserve(endIndex - startIndex);
            for (size_t i = startIndex; i < endIndex; ++i) {
                const T &primitive = primitives[i];
                AABB &&box = primitive.getAABB();
                buildNodes.emplace_back(std::move(box), static_cast<int>(i));
            }

            // Here, build Nodes of this segment
            // You may add code to build the BVH for this segment based on your design
        }));
    }

    // Wait for all threads to finish
    for (auto &f : futures) {
        f.get();
    }
    
    // Now you can merge results from the threads and build the actual BVH
    // Note: Please add proper synchronization and data gathering logic for merging built nodes if needed.

    // ... [Finalization code remains unchanged]
}

// ... [Rest of the class remains unchanged]

} // namespace MobileRT

#endif //MOBILERT_ACCELERATORS_BVH_HPP
