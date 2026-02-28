#include <thread>
#include <future>
#include <vector>
#include <algorithm>

// ... other includes and code remain unchanged ...

private:
    void buildParallel(::std::vector<T> &&primitives);
    void buildChunk(const std::vector<T>& primitives, std::vector<BVHNode>& localBoxes, int begin, int end);

public:
    explicit BVH(::std::vector<T> &&primitives);
};

// ... existing constructor ...

template<typename T>
void BVH<T>::buildParallel(::std::vector<T> &&primitives) {
    const std::size_t numThreads = std::thread::hardware_concurrency(); // Get the number of available threads
    const std::size_t primitivesSize = primitives.size();
    
    std::vector<std::future<void>> futures;
    std::vector<std::vector<BVHNode>> localBoxes(numThreads);

    size_t chunkSize = primitivesSize / numThreads;

    for (size_t i = 0; i < numThreads; ++i) {
        size_t begin = i * chunkSize;
        size_t end = (i == numThreads - 1) ? primitivesSize : begin + chunkSize;

        // Launch a thread for each chunk
        futures.emplace_back(std::async(std::launch::async, &BVH<T>::buildChunk, this, std::cref(primitives), std::ref(localBoxes[i]), begin, end));
    }

    // Wait for all threads to finish
    for (auto& future : futures) {
        future.get();
    }

    // Merge all local boxes into the main boxes_ vector
    for (const auto& boxChunk : localBoxes) {
        this->boxes_.insert(this->boxes_.end(), boxChunk.begin(), boxChunk.end());
    }

    // Process the merged boxes_ vector (sorting, layout, etc.)
    // You may want to call a merge function here, depending on your original algorithm.
}

// Process each chunk independently
template<typename T>
void BVH<T>::buildChunk(const std::vector<T>& primitives, std::vector<BVHNode>& localBoxes, int begin, int end) {
    for (int i = begin; i < end; ++i) {
        AABB box = primitives[i].getAABB();
        localBoxes.emplace_back(BVHNode{ box, i, 1 }); // Simple placeholder logic for localNodes
    }
    
    // Additional logic to add localBoxes to a global structure might need adjustment
}

// Don't forget to adjust the constructor to call buildParallel instead of build
template<typename T>
BVH<T>::BVH(::std::vector<T> &&primitives) {
    if (primitives.empty()) {
        this->boxes_.emplace_back();
        LOG_WARN("Empty BVH for '", typeid(T).name(), "' without any primitives.");
        return;
    }
    LOG_INFO("Building BVH for '", typeid(T).name(), "' with '", primitives.size(), "' primitives.");
    buildParallel(std::move(primitives));
    LOG_INFO("Built BVH for '", typeid(T).name(), "' with '", this->primitives_.size(), "' primitives in '", this->boxes_.size(), "' boxes.");
}

// ... existing methods remain unchanged ...
