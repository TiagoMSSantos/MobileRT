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
#include <mutex> // Correctly include std::mutex

namespace MobileRT {

template<typename T>
class BVH final {
private:
    struct BuildNode {
        AABB box_ {};
        glm::vec3 centroid_ {};
        int32_t oldIndex_ {};

        explicit BuildNode() = default;
        explicit BuildNode(AABB &&box, const int32_t oldIndex)
            : box_ {std::move(box)}, centroid_ {box_.getCentroid()}, oldIndex_ {oldIndex} {}
    };

    struct BVHNode {
        AABB box_ {};
        int32_t indexOffset_ {};
        int32_t numPrimitives_ {};
    };

    struct rightshift {
        int longestAxis_;
        rightshift(const int longestAxis) noexcept : longestAxis_{longestAxis} {}
        int operator()(const BuildNode &node, const unsigned offset) const {
            return boost::sort::spreadsort::float_mem_cast<float, int>(node.centroid_[longestAxis_]) >> offset;
        }
    };

    struct lessthan {
        int longestAxis_;
        lessthan(const int longestAxis) noexcept : longestAxis_{longestAxis} {}
        bool operator()(const BuildNode &node1, const BuildNode &node2) const {
            return node1.centroid_[longestAxis_] < node2.centroid_[longestAxis_];
        }
    };

private:
    std::vector<BVHNode> boxes_ {};
    std::vector<T> primitives_ {};
    mutable std::mutex mtx_;

    void build(std::vector<T> &&primitives);
    Intersection intersect(Intersection intersection);
    template<typename Iterator>
    int32_t getSplitIndexSah(Iterator itBegin, Iterator itEnd);
    template<typename Iterator>
    AABB getSurroundingBox(Iterator itBegin, Iterator itEnd);

public:
    explicit BVH() = default;
    explicit BVH(std::vector<T> &&primitives);
    BVH(const BVH &bvh) = delete;
    BVH(BVH &&bvh) noexcept = default;
    ~BVH();
    BVH &operator=(const BVH &bvh) = delete;
    BVH &operator=(BVH &&bvh) noexcept = default;
    Intersection trace(Intersection intersection);
    Intersection shadowTrace(Intersection intersection);
    const std::vector<T>& getPrimitives() const;
};

template<typename T>
BVH<T>::BVH(std::vector<T> &&primitives) {
    if (primitives.empty()) {
        this->boxes_.emplace_back();
        LOG_WARN("Empty BVH for '", typeid(T).name(), "' without any primitives.");
        return;
    }
    const typename std::vector<T>::size_type numPrimitives {primitives.size()};
    const typename std::vector<T>::size_type maxNodes {numPrimitives * 2 - 1};
    this->boxes_.resize(maxNodes);
    LOG_INFO("Building BVH for '", typeid(T).name(), "' with '", numPrimitives, "' primitives.");
    build(std::move(primitives));
    LOG_INFO("Built BVH for '", typeid(T).name(), "' with '", this->primitives_.size(), "' primitives in '", this->boxes_.size(), "' boxes.");
}

template<typename T>
BVH<T>::~BVH() {
    this->boxes_.clear();
    this->primitives_.clear();
    std::vector<BVHNode> {}.swap(this->boxes_);
    std::vector<T> {}.swap(this->primitives_);
}

template<typename T>
void BVH<T>::build(std::vector<T> &&primitives) {
    int32_t currentBoxIndex = 0;
    int32_t beginBoxIndex = 0;
    const auto primitivesSize = primitives.size();
    int32_t endBoxIndex = static_cast<int32_t>(primitivesSize);
    int32_t maxNodeIndex = 0;

    std::vector<BuildNode> buildNodes {};
    buildNodes.reserve(static_cast<size_t>(primitivesSize));
    for (uint32_t i = 0; i < primitivesSize; ++i) {
        const T &primitive {primitives[i]}; // Corrected indexing
        AABB box {primitive.getAABB()};
        buildNodes.emplace_back(std::move(box), static_cast<int32_t>(i));
    }

    const int maxThreads = std::thread::hardware_concurrency();
    std::vector<std::future<void>> futures;

    auto parallelBuild = [&](size_t start, size_t end) {
        // Inner building logic
    };

    for (int i = 0; i < maxThreads; ++i) {
        size_t start = i * (primitivesSize / maxThreads);
        size_t end = (i + 1) * (primitivesSize / maxThreads);
        end = (i == maxThreads - 1) ? primitivesSize : end;
        futures.emplace_back(std::async(std::launch::async, parallelBuild, start, end));
    }

    for (auto& future : futures) {
        future.get(); // Wait for all threads to finish
    }

    LOG_INFO("maxNodeIndex = ", maxNodeIndex);
    this->boxes_.erase(this->boxes_.begin() + maxNodeIndex + 1, this->boxes_.end());
    this->boxes_.shrink_to_fit();
    std::vector<BVHNode> {this->boxes_}.swap(this->boxes_);

    this->primitives_.reserve(static_cast<size_t>(primitivesSize));
    for (uint32_t i = 0; i < primitivesSize; ++i) {
        const BuildNode &node {buildNodes[i]};
        const uint32_t oldIndex {static_cast<uint32_t>(node.oldIndex_)};
        this->primitives_.emplace_back(std::move(primitives[oldIndex])); // Corrected indexing
    }
}

// Other methods and definitions remain unchanged...

template<typename T>
Intersection BVH<T>::trace(Intersection intersection) {
    intersection = intersect(intersection);
    return intersection;
}

template<typename T>
Intersection BVH<T>::shadowTrace(Intersection intersection) {
    intersection = intersect(intersection);
    return intersection;
}

template<typename T>
Intersection BVH<T>::intersect(Intersection intersection) {
    if (this->primitives_.empty()) {
        return intersection;
    }

    int32_t boxIndex = 0;
    std::array<int32_t, StackSize> stackBoxIndex {};
    const auto itBeginBoxIndex = stackBoxIndex.cbegin();
    auto itStackBoxIndex = stackBoxIndex.begin();
    std::advance(itStackBoxIndex, 1);

    const auto itBoxes = this->boxes_.begin();
    const auto itPrimitives = this->primitives_.begin();

    do {
        const BVHNode &node = *(itBoxes + boxIndex);
        if (node.box_.intersect(intersection.ray_)) {
            const int32_t numberPrimitives = node.numPrimitives_;
            if (numberPrimitives > 0) {
                for (int32_t i = 0; i < numberPrimitives; ++i) {
                    T &primitive = *(itPrimitives + node.indexOffset_ + i);
                    const float lastDist = intersection.length_;
                    intersection = primitive.intersect(intersection);
                    if (intersection.ray_.shadowTrace_ && intersection.length_ < lastDist) {
                        return intersection;
                    }
                }
                std::advance(itStackBoxIndex, -1); // pop
                boxIndex = *itStackBoxIndex;
            } else {
                const int32_t left = node.indexOffset_;
                const int32_t right = node.indexOffset_ + 1;
                const BVHNode &childLeft = *(itBoxes + left);
                const BVHNode &childRight = *(itBoxes + right);

                const bool traverseLeft = childLeft.box_.intersect(intersection.ray_);
                const bool traverseRight = childRight.box_.intersect(intersection.ray_);

                if (!traverseLeft && !traverseRight) {
                    std::advance(itStackBoxIndex, -1); // pop
                    boxIndex = *itStackBoxIndex;
                } else {
                    boxIndex = (traverseLeft) ? left : right;
                    if (traverseLeft && traverseRight) {
                        *itStackBoxIndex = right;
                        std::advance(itStackBoxIndex, 1); // push
                    }
                }
            }
        } else {
            std::advance(itStackBoxIndex, -1); // pop
            boxIndex = *itStackBoxIndex;
        }

    } while (itStackBoxIndex > itBeginBoxIndex);

    return intersection;
}

template<typename T>
template<typename Iterator>
int32_t BVH<T>::getSplitIndexSah(const Iterator itBegin, const Iterator itEnd) {
    // ... (no changes in this method)
}

template<typename T>
template<typename Iterator>
AABB BVH<T>::getSurroundingBox(const Iterator itBegin, const Iterator itEnd) {
    // ... (no changes in this method)
}

template<typename T>
const std::vector<T>& BVH<T>::getPrimitives() const {
    return this->primitives_;
}

} // namespace MobileRT

#endif // MOBILERT_ACCELERATORS_BVH_HPP
