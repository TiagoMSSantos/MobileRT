#include <future>
#include <thread>
#include <vector>

namespace MobileRT {

template<typename T>
void BVH<T>::build(::std::vector<T> &&primitives) {
    ::std::int32_t currentBoxIndex {};
    ::std::int32_t beginBoxIndex {};
    const long long unsigned primitivesSize {primitives.size()};
    ::std::int32_t endBoxIndex {static_cast<::std::int32_t>(primitivesSize)};
    ::std::int32_t maxNodeIndex {};

    ::std::array<::std::int32_t, StackSize> stackBoxIndex {};
    ::std::array<::std::int32_t, StackSize> stackBoxBegin {};
    ::std::array<::std::int32_t, StackSize> stackBoxEnd {};

    ::std::array<::std::int32_t, StackSize>::iterator itStackBoxIndex {stackBoxIndex.begin()};
    ::std::advance(itStackBoxIndex, 1);

    ::std::array<::std::int32_t, StackSize>::iterator itStackBoxBegin {stackBoxBegin.begin()};
    ::std::advance(itStackBoxBegin, 1);

    ::std::array<::std::int32_t, StackSize>::iterator itStackBoxEnd {stackBoxEnd.begin()};
    ::std::advance(itStackBoxEnd, 1);

    const ::std::array<::std::int32_t, StackSize>::const_iterator itStackBoxIndexBegin {stackBoxIndex.cbegin()};

    // Auxiliary structure used to sort all the AABBs by the position of the centroid.
    ::std::vector<BuildNode> buildNodes {};
    buildNodes.reserve(static_cast<long unsigned>(primitivesSize));
    for (::std::uint32_t i {}; i < primitivesSize; ++i) {
        const T &primitive {primitives[i]};
        AABB &&box {primitive.getAABB()};
        buildNodes.emplace_back(::std::move(box), static_cast<::std::int32_t>(i));
    }

    // Split the work into several tasks
    const int numThreads = std::thread::hardware_concurrency();
    const size_t numBuildNodes = buildNodes.size();
    const size_t chunkSize = (numBuildNodes + numThreads - 1) / numThreads;
    std::vector<std::future<void>> futures;

    for (int i = 0; i < numThreads; ++i) {
        futures.emplace_back(std::async(std::launch::async, [this, &buildNodes, beginBoxIndex, endBoxIndex, currentBoxIndex]() {
            std::unique_lock<std::mutex> lock(mutex_); // Ensure thread-safe access to shared resources

            size_t start = i * chunkSize;
            size_t end = std::min(start + chunkSize, buildNodes.size());

            for (size_t j = start; j < end; ++j) {
                const BuildNode &node = buildNodes[j];
                AABB newBox = node.box_;
                if (j == start) {
                    this->boxes_[currentBoxIndex].box_ = newBox;
                } else {
                    this->boxes_[currentBoxIndex].box_ = surroundingBox(this->boxes_[currentBoxIndex].box_, newBox);
                }

                // Further construction logic can go here if needed
            }

            // Manage other shared resources for currentBoxIndex, beginBoxIndex, endBoxIndex etc.
        }));
    }

    for (auto &fut : futures) {
        fut.get(); // Wait for all threads to finish
    }

    // Continue with the rest of the build process...
    LOG_INFO("maxNodeIndex = ", maxNodeIndex);
    this->boxes_.erase(this->boxes_.begin() + maxNodeIndex + 1, this->boxes_.end());
    this->boxes_.shrink_to_fit();
    ::std::vector<BVHNode> {this->boxes_}.swap(this->boxes_);

    // Insert primitives with the proper order.
    this->primitives_.reserve(static_cast<long unsigned>(primitivesSize));
    for (::std::uint32_t i {}; i < primitivesSize; ++i) {
        const BuildNode &node {buildNodes[i]};
        const ::std::uint32_t oldIndex {static_cast<::std::uint32_t>(node.oldIndex_)};
        this->primitives_.emplace_back(::std::move(primitives[oldIndex]));
    }
}
    
} // namespace MobileRT
