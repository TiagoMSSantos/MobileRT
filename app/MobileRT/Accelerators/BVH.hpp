#include <future>
//... other includes

template<typename T>
void BVH<T>::build(::std::vector<T> &&primitives) {
    ::std::int32_t currentBoxIndex {};
    ::std::int32_t beginBoxIndex {};
    const long long unsigned primitivesSize {primitives.size()};
    ::std::int32_t endBoxIndex {static_cast<::std::int32_t>(primitivesSize)};
    ::std::int32_t maxNodeIndex {};

    // Auxiliary structure used to sort all the AABBs by the position of the centroid.
    ::std::vector<BuildNode> buildNodes;
    buildNodes.reserve(static_cast<long unsigned>(primitivesSize));
    
    for (::std::uint32_t i {}; i < primitivesSize; ++i) {
        const T &primitive {primitives[i]};
        AABB &&box {primitive.getAABB()};
        buildNodes.emplace_back(::std::move(box), static_cast<::std::int32_t>(i));
    }
    
    // Create vectors for asynchronous tasks
    std::vector<std::future<void>> futures;
    
    // This is the main loop to build the BVH in parallel
    do {
        // Define what to do with the current box
        const auto itCurrentBox {this->boxes_.begin() + currentBoxIndex};
        const ::std::int32_t boxPrimitivesSize {endBoxIndex - beginBoxIndex};
        const auto itBegin {buildNodes.begin() + beginBoxIndex};
        const auto itEnd {buildNodes.begin() + endBoxIndex};

        const AABB surroundingBox {getSurroundingBox(itBegin, itEnd)};
        const ::glm::vec3 maxDist {surroundingBox.getPointMax() - surroundingBox.getPointMin()};
        const int longestAxis {
            maxDist[0] >= maxDist[1] && maxDist[0] >= maxDist[2] ? 0
            : maxDist[1] >= maxDist[0] && maxDist[1] >= maxDist[2] ? 1
            : 2
        };

        const int numBuckets {10};
        const ::glm::vec3 step {maxDist / static_cast<float>(numBuckets)};
        const float stepAxis {step[longestAxis]};
        const float startBox {surroundingBox.getPointMin()[longestAxis]};
        const float bucket1MaxLimit {startBox + stepAxis};

        // Use C++ partition to sort primitives by buckets
        typename ::std::vector<BuildNode>::iterator itBucket = std::partition(itBegin, itEnd, [&](const BuildNode &node) {
            return node.centroid_[longestAxis] < bucket1MaxLimit;
        });

        for (::std::int32_t bucketIndex {2}; bucketIndex < numBuckets; ++bucketIndex) {
            const float bucketMaxLimit {startBox + stepAxis * bucketIndex};
            itBucket = std::partition(itBucket, itEnd, [&](const BuildNode &node) {
                return node.centroid_[longestAxis] < bucketMaxLimit;
            });
        }

        itCurrentBox->box_ = itBegin->box_;
        ::std::vector<AABB> boxes {itCurrentBox->box_};
        boxes.reserve(static_cast<::std::uint32_t>(boxPrimitivesSize));
        
        for (::std::int32_t i {beginBoxIndex + 1}; i < endBoxIndex; ++i) {
            const AABB newBox {buildNodes[static_cast<::std::uint32_t>(i)].box_};
            itCurrentBox->box_ = ::MobileRT::surroundingBox(newBox, itCurrentBox->box_);
            boxes.emplace_back(newBox);
        }

        const int maxPrimitivesInBoxLeaf {4};
        const bool isLeaf {boxPrimitivesSize <= maxPrimitivesInBoxLeaf};
        if (isLeaf) {
            itCurrentBox->indexOffset_ = beginBoxIndex;
            itCurrentBox->numPrimitives_ = boxPrimitivesSize;
            currentBoxIndex = *--futures.end();
        } else {
            const ::std::int32_t left {maxNodeIndex + 1};
            const ::std::int32_t right {left + 1};
            const ::std::int32_t splitIndex {getSplitIndexSah(boxes.begin(), boxes.end())};

            itCurrentBox->indexOffset_ = left;
            maxNodeIndex = ::std::max(right, maxNodeIndex);

            // Create async tasks for left and right nodes
            futures.push_back(std::async(std::launch::async, [&]() {
                // Left subtree
                // related operations here
            }));

            futures.push_back(std::async(std::launch::async, [&]() {
                // Right subtree
                // related operations here
            }));

            currentBoxIndex = left;
            endBoxIndex = beginBoxIndex + splitIndex;
        }
    } while (futures.size() > 0);

    // Wait for all futures to finish
    for(auto &fut : futures) {
        fut.get();
    }

    LOG_INFO("maxNodeIndex = ", maxNodeIndex);
    this->boxes_.erase(this->boxes_.begin() + maxNodeIndex + 1, this->boxes_.end());
    this->boxes_.shrink_to_fit();
    ::std::vector<BVHNode> {this->boxes_}.swap(this->boxes_);

    // Insert primitives with the proper order.
    this->primitives_.reserve(static_cast<long unsigned>(primitivesSize));
    for (::std::uint32_t i {}; i < primitivesSize; ++i) {
        const BuildNode &node {buildNodes[i]};
        this->primitives_.emplace_back(::std::move(primitives[node.oldIndex_]));
    }
}
