#include <thread>
#include <future>
#include <mutex>

// ... (other includes and namespace MobileRT)

template<typename T>
void BVH<T>::build(::std::vector<T> &&primitives) {
    // ... (initializations and preparations)

    const unsigned numThreads = std::thread::hardware_concurrency();
    std::vector<std::future<void>> futures;
    std::mutex mutex;

    // Divide the input primitives into chunks for each thread
    const size_t chunkSize = primitives.size() / numThreads;
    std::vector<std::vector<T>> chunks(numThreads);

    for (size_t i = 0; i < numThreads; ++i) {
        size_t start = i * chunkSize;
        size_t end = (i == numThreads - 1) ? primitives.size() : start + chunkSize;
        chunks[i] = std::vector<T>(primitives.begin() + start, primitives.begin() + end);
        
        futures.emplace_back(std::async(std::launch::async, [this, &mutex, i]() {
            // Build BVH for the given chunk in parallel
            // Lock mutex only when interacting with shared resources
            std::lock_guard<std::mutex> guard(mutex);
            // Construct and add BVH nodes for this chunk
            // Implementation here...
        }));
    }

    // Wait for all threads to finish
    for (auto &future : futures) {
        future.get();
    }

    // Combine the results in `this->boxes_` and `this->primitives_`
    // Implementation here...

    LOG_INFO("maxNodeIndex = ", maxNodeIndex);
    this->boxes_.erase(this->boxes_.begin() + maxNodeIndex + 1, this->boxes_.end());
    this->boxes_.shrink_to_fit();
    ::std::vector<BVHNode> {this->boxes_}.swap(this->boxes_);

    // Insert primitives with the proper order.
    this->primitives_.reserve(static_cast<long unsigned>(primitivesSize));
    for (const auto &chunk : chunks) {
        for (size_t i = 0; i < chunk.size(); ++i) {
            const BuildNode &node {buildNodes[i]}; // Adjust as per your logic
            const ::std::uint32_t oldIndex {static_cast<::std::uint32_t>(node.oldIndex_)};
            this->primitives_.emplace_back(::std::move(chunk[oldIndex]));
        }
    }

    LOG_INFO("Finished building BVH with parallelization.");
}

// ... (rest of the BVH class implementation)
