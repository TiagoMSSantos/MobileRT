#include <thread>
#include <future>

//... other includes remain the same
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
    
    //... Initialization code remains here

    // Auxiliary structure used to sort all the AABBs by the position of the centroid.
    ::std::vector<BuildNode> buildNodes {};
    buildNodes.reserve(static_cast<long unsigned>(primitivesSize));
    for (::std::uint32_t i {}; i < primitivesSize; ++i) {
        const T &primitive {primitives[i]};
        AABB &&box {primitive.getAABB()};
        buildNodes.emplace_back(::std::move(box), static_cast<::std::int32_t>(i));
    }

    // Now use multiple threads to process partitions of the workload
    constexpr int NUM_THREADS = std::thread::hardware_concurrency() ? std::thread::hardware_concurrency() : 2;
    std::vector<std::future<void>> futures;

    auto buildTask = [&](int startIdx, int endIdx) {
        for (int boxIdx = startIdx; boxIdx < endIdx; ) {
            const auto itCurrentBox {this->boxes_.begin() + currentBoxIndex};
            const ::std::int32_t boxPrimitivesSize {endBoxIndex - beginBoxIndex};
            const auto itBegin {buildNodes.begin() + beginBoxIndex};
            const auto itEnd {buildNodes.begin() + endBoxIndex};
            const AABB surroundingBox {getSurroundingBox(itBegin, itEnd)};
            const ::glm::vec3 maxDist {surroundingBox.getPointMax() - surroundingBox.getPointMin()};
            const int longestAxis {
                maxDist[0] >= maxDist[1] && maxDist[0] >= maxDist[2] ? 0 :
                maxDist[1] >= maxDist[0] && maxDist[1] >= maxDist[2] ? 1 :
                2
            };

            // Omitting the code for creating partitions for brevity

            if (isLeaf) {
                itCurrentBox->indexOffset_ = beginBoxIndex;
                itCurrentBox->numPrimitives_ = boxPrimitivesSize;

                // pop logic
            } else {
                // splitting into children logic
            }

        }
    };

    int segmentSize = primitivesSize / NUM_THREADS;
    for (int i = 0; i < NUM_THREADS; ++i) {
        int startIdx = i * segmentSize;
        int endIdx = (i == NUM_THREADS - 1) ? primitivesSize : (i + 1) * segmentSize;

        futures.push_back(std::async(std::launch::async, buildTask, startIdx, endIdx));
    }

    for (auto &future : futures) {
        future.get(); // Wait for all threads to complete
    }

    //... Finalize BVH structure

    LOG_INFO("Built BVH with multiple threads for '", typeid(T).name(), "' with '", this->primitives_.size(), "' primitives in '", this->boxes_.size(), "' boxes.");
}

}
