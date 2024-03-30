#ifndef MOBILERT_ACCELERATORS_BVH_HPP
#define MOBILERT_ACCELERATORS_BVH_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Scene.hpp"
#include <algorithm>
#include <array>
#include <boost/sort/spreadsort/spreadsort.hpp>
#include <glm/glm.hpp>
#include <random>
#include <vector>

namespace MobileRT {

    /**
     * A class which represents the Bounding Volume Hierarchy acceleration structure.
     *
     * @tparam T The type of the primitives.
     */
    template<typename T>
    class BVH final {
        private:
            /**
             * An auxiliary node used for the construction of the BVH.
             * It is used to sort all the AABBs by the position of the centroid.
             */
            struct BuildNode {
                AABB box_ {};
                ::glm::vec3 centroid_ {};
                ::std::int32_t oldIndex_ {};

                /**
                 * The constructor.
                 */
                explicit BuildNode() = default;

                /**
                 * The constructor.
                 *
                 * @param box      The box to store in the node.
                 * @param oldIndex The old index of the box in the original vector (used to put the box in the proper
                 * position.
                 */
                explicit BuildNode(AABB &&box, const ::std::int32_t oldIndex) :
                    box_ {box},
                    centroid_ {box_.getCentroid()},
                    oldIndex_ {oldIndex} {

                }
            };

            /**
             * A node of the BVH vector.
             */
            struct BVHNode {
                AABB box_ {};
                ::std::int32_t indexOffset_ {};
                ::std::int32_t numPrimitives_ {};
            };

            struct rightshift {
                int longestAxis_;
                rightshift(const int longestAxis) noexcept : longestAxis_{longestAxis} { }

                int operator()(const BuildNode &node, const unsigned offset) const {
                    return ::boost::sort::spreadsort::float_mem_cast<float, int>(node.centroid_[this->longestAxis_]) >> offset;
                }
            };

            struct lessthan {
                int longestAxis_;
                lessthan(const int longestAxis) noexcept : longestAxis_{longestAxis} { }

                bool operator()(const BuildNode &node1, const BuildNode &node2) const {
                    return node1.centroid_[this->longestAxis_] < node2.centroid_[this->longestAxis_];
                }
            };

        private:
            ::std::vector<BVHNode> boxes_ {};
            ::std::vector<T> primitives_ {};

        private:
            void build(::std::vector<T> &&primitives);

            void build(::std::int32_t currentBoxIndex,
                       ::std::int32_t beginBoxIndex,
                       ::std::int32_t endBoxIndex,
                       ::std::vector<BuildNode> &buildNodes,
                       const ::std::array<::std::int32_t, StackSize>::const_iterator itStackBoxIndexBegin,
                       ::std::array<::std::int32_t, StackSize>::iterator itStackBoxIndex,
                       ::std::array<::std::int32_t, StackSize>::iterator itStackBoxBegin,
                       ::std::array<::std::int32_t, StackSize>::iterator itStackBoxEnd,
                       ::std::int32_t *maxNodeIndex);

            Intersection intersect(Intersection intersection);

            template<typename Iterator>
            ::std::int32_t getSplitIndexSah(Iterator itBegin, Iterator itEnd);

            template<typename Iterator>
            AABB getSurroundingBox(Iterator itBegin, Iterator itEnd);

        public:
            explicit BVH() = default;

            explicit BVH(::std::vector<T> &&primitives);

            BVH(const BVH &bvh) = delete;

            BVH(BVH &&bvh) noexcept = default;

            ~BVH();

            BVH &operator=(const BVH &bvh) = delete;

            BVH &operator=(BVH &&bvh) noexcept = default;

            Intersection trace(Intersection intersection);

            Intersection shadowTrace(Intersection intersection);

            const ::std::vector<T>& getPrimitives() const;
    };



    /**
     * The constructor.
     *
     * @tparam T The type of the primitives.
     * @param primitives The vector containing all the primitives to store in the BVH.
     */
    template<typename T>
    BVH<T>::BVH(::std::vector<T> &&primitives) {
        if (primitives.empty()) {
            this->boxes_.emplace_back(BVHNode {});
            return;
        }
        LOG_INFO("Building BVH for: ", typeid(T).name());
        const typename ::std::vector<T>::size_type numPrimitives {primitives.size()};
        const typename ::std::vector<T>::size_type maxNodes {numPrimitives * 2 - 1};
        this->boxes_.resize(maxNodes);
        build(::std::move(primitives));
    }

    /**
     * The destructor.
     *
     * @tparam T The type of the primitives.
     */
    template<typename T>
    BVH<T>::~BVH() {
        this->boxes_.clear();
        this->primitives_.clear();

        ::std::vector<BVHNode> {}.swap(this->boxes_);
        ::std::vector<T> {}.swap(this->primitives_);
    }

    /**
     * A helper method which builds the BVH structure.
     *
     * @tparam T The type of the primitives.
     * @param primitives A vector containing all the primitives to store in the BVH.
     */
    template<typename T>
    void BVH<T>::build(::std::vector<T> &&primitives) {
        ::std::int32_t currentBoxIndex {};
        ::std::int32_t beginBoxIndex {};
        const long long unsigned primitivesSize {primitives.size()};
        ::std::int32_t endBoxIndex {static_cast<::std::int32_t> (primitivesSize)};
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
        buildNodes.reserve(static_cast<long unsigned> (primitivesSize));
        for (::std::uint32_t i {}; i < primitivesSize; ++i) {
            const T &primitive {primitives [i]};
            AABB &&box {primitive.getAABB()};
            buildNodes.emplace_back(BuildNode {::std::move(box), static_cast<::std::int32_t> (i)});
        }

        LOG_INFO("Building BVH");
        build(currentBoxIndex, beginBoxIndex, endBoxIndex, buildNodes, itStackBoxIndexBegin, itStackBoxIndex, itStackBoxBegin, itStackBoxEnd, &maxNodeIndex);

        LOG_INFO("maxNodeIndex = ", maxNodeIndex);
        this->boxes_.erase (this->boxes_.begin() + maxNodeIndex + 1, this->boxes_.end());
        this->boxes_.shrink_to_fit();
        ::std::vector<BVHNode> {this->boxes_}.swap(this->boxes_);

        LOG_INFO("Inserting primitives into BVH with the proper order.");
        this->primitives_.reserve(static_cast<long unsigned> (primitivesSize));
        for (::std::uint32_t i {}; i < primitivesSize; ++i) {
            const BuildNode &node {buildNodes[i]};
            const ::std::uint32_t oldIndex {static_cast<::std::uint32_t> (node.oldIndex_)};
            this->primitives_.emplace_back(::std::move(primitives[oldIndex]));
        }
    }

    /**
     * Build the BVH structure.
     * 
     * @tparam T The type of the primitives.
     */
    template<typename T>
    void BVH<T>::build(::std::int32_t currentBoxIndex,
                       ::std::int32_t beginBoxIndex,
                       ::std::int32_t endBoxIndex,
                       ::std::vector<BuildNode> &buildNodes,
                       const ::std::array<::std::int32_t, StackSize>::const_iterator itStackBoxIndexBegin,
                       ::std::array<::std::int32_t, StackSize>::iterator itStackBoxIndex,
                       ::std::array<::std::int32_t, StackSize>::iterator itStackBoxBegin,
                       ::std::array<::std::int32_t, StackSize>::iterator itStackBoxEnd,
                       ::std::int32_t *maxNodeIndex) {
        do {
            const auto itCurrentBox {this->boxes_.begin() + currentBoxIndex};
            const ::std::int32_t boxPrimitivesSize {endBoxIndex - beginBoxIndex};
            const auto itBegin {buildNodes.begin() + beginBoxIndex};

            const auto itEnd {buildNodes.begin() + endBoxIndex};
            const AABB surroundingBox {getSurroundingBox(itBegin, itEnd)};
            const ::glm::vec3 maxDist {surroundingBox.getPointMax() - surroundingBox.getPointMin()};
            const int longestAxis {
                maxDist[0] >= maxDist[1] && maxDist[0] >= maxDist[2]
                ? 0
                : maxDist[1] >= maxDist[0] && maxDist[1] >= maxDist[2]
                    ? 1
                    : 2
            };

            // Use C++ partition to sort primitives by buckets where each bucket don't have primitives sorted inside.
            // It is faster than using C++ standard sort or C++ Boost Radix sort.
            const int numBuckets {10};
            const ::glm::vec3 step {maxDist / static_cast<float> (numBuckets)};
            const float stepAxis {step[longestAxis]};
            const float startBox {surroundingBox.getPointMin()[longestAxis]};
            const float bucket1MaxLimit {startBox + stepAxis};
            typename ::std::vector<BuildNode>::iterator itBucket {::std::partition(itBegin, itEnd,
                [&](const BuildNode &node) {
                    return node.centroid_[longestAxis] < bucket1MaxLimit;
                }
            )};
            for (::std::int32_t bucketIndex {2}; bucketIndex < numBuckets; ++bucketIndex) {
                const float bucketMaxLimit {startBox + stepAxis * bucketIndex};
                itBucket = ::std::partition(::std::move(itBucket), itEnd,
                    [&](const BuildNode &node) {
                        return node.centroid_[longestAxis] < bucketMaxLimit;
                    }
                );
            }


            itCurrentBox->box_ = itBegin->box_;
            ::std::vector<AABB> boxes {itCurrentBox->box_};
            boxes.reserve(static_cast<::std::uint32_t> (boxPrimitivesSize));
            for (::std::int32_t i {beginBoxIndex + 1}; i < endBoxIndex; ++i) {
                const AABB newBox {buildNodes[static_cast<::std::uint32_t> (i)].box_};
                itCurrentBox->box_ = ::MobileRT::surroundingBox(newBox, itCurrentBox->box_);
                boxes.emplace_back(newBox);
            }

            const int maxPrimitivesInBoxLeaf {4};
            const bool isLeaf {boxPrimitivesSize <= maxPrimitivesInBoxLeaf};
            if (isLeaf) {
                itCurrentBox->indexOffset_ = beginBoxIndex;
                itCurrentBox->numPrimitives_ = boxPrimitivesSize;

                LOG_INFO("Pop stacks");
                ::std::advance(itStackBoxIndex, -1); // pop
                // currentBoxIndex = *itStackBoxIndex;
                ::std::advance(itStackBoxBegin, -1); // pop
                // beginBoxIndex = *itStackBoxBegin;
                ::std::advance(itStackBoxEnd, -1); // pop
                // endBoxIndex = *itStackBoxEnd;
            } else {
                const ::std::int32_t left {*maxNodeIndex + 1};
                const ::std::int32_t right {left + 1};
                const ::std::int32_t splitIndex {getSplitIndexSah(boxes.begin(), boxes.end())};

                itCurrentBox->indexOffset_ = left;
                *maxNodeIndex = ::std::max(right, *maxNodeIndex);

                *itStackBoxIndex = right;
                *itStackBoxBegin = beginBoxIndex + splitIndex;
                *itStackBoxEnd = endBoxIndex;

                build(left, beginBoxIndex, beginBoxIndex + splitIndex, buildNodes, itStackBoxIndexBegin, itStackBoxIndex + 1, itStackBoxBegin + 1, itStackBoxEnd + 1, maxNodeIndex);
            }
        } while(itStackBoxIndex > itStackBoxIndexBegin);
    }

    /**
     * This method casts a ray into the geometry and calculates the nearest intersection point from the origin of the
     * ray.
     *
     * @tparam T The type of the primitives.
     * @param intersection The current intersection of the ray with previous primitives.
     * @return The intersection of the ray with the geometry.
     */
    template<typename T>
    Intersection BVH<T>::trace(Intersection intersection) {
        intersection = intersect(intersection);
        return intersection;
    }

    /**
     * This method casts a ray into the geometry and calculates a random intersection point.
     * The intersection point itself is not important, the important is to determine if the ray intersects some
     * primitive in the scene or not.
     *
     * @tparam T The type of the primitives.
     * @param intersection The current intersection of the ray with previous primitives.
     * @return The intersection of the ray with the geometry.
     */
    template<typename T>
    Intersection BVH<T>::shadowTrace(Intersection intersection) {
        intersection = intersect(intersection);
        return intersection;
    }

    /**
     * Helper method which calculates the intersection point from the origin of the ray.
     * <br>
     * This method supports two modes:<br>
     *  - trace the ray until finding the nearest intersection point from the origin of the ray<br>
     *  - trace the ray until finding any intersection point from the origin of the ray<br>
     *
     * @tparam T The type of the primitives.
     * @param intersection The previous intersection point of the ray (used to update its data in case it is found a
     * nearest intersection point.
     * @return The intersection point of the ray in the scene.
     */
    template<typename T>
    Intersection BVH<T>::intersect(Intersection intersection) {
        if (this->primitives_.empty()) {
            return intersection;
        }
        ::std::int32_t boxIndex {};
        ::std::array<::std::int32_t, StackSize> stackBoxIndex {};

        const ::std::array<::std::int32_t, StackSize>::const_iterator itBeginBoxIndex {stackBoxIndex.cbegin()};
        ::std::array<::std::int32_t, StackSize>::iterator itStackBoxIndex {stackBoxIndex.begin()};
        ::std::advance(itStackBoxIndex, 1);

        const typename ::std::vector<BVHNode>::iterator itBoxes {this->boxes_.begin()};
        const typename ::std::vector<T>::iterator itPrimitives {this->primitives_.begin()};
        do {
            const BVHNode &node {*(itBoxes + boxIndex)};
            if (node.box_.intersect(intersection.ray_)) {

                const ::std::int32_t numberPrimitives {node.numPrimitives_};
                if (numberPrimitives > 0) {
                    for (::std::int32_t i {}; i < numberPrimitives; ++i) {
                        T &primitive {*(itPrimitives + node.indexOffset_ + i)};
                        const float lastDist {intersection.length_};
                        intersection = primitive.intersect(intersection);
                        if (intersection.ray_.shadowTrace_ && intersection.length_ < lastDist) {
                            return intersection;
                        }
                    }
                    ::std::advance(itStackBoxIndex, -1); // pop
                    boxIndex = *itStackBoxIndex;
                } else {
                    const ::std::int32_t left {node.indexOffset_};
                    const ::std::int32_t right {node.indexOffset_ + 1};
                    const BVHNode &childLeft {*(itBoxes + left)};
                    const BVHNode &childRight {*(itBoxes + right)};

                    const bool traverseLeft {childLeft.box_.intersect(intersection.ray_)};
                    const bool traverseRight {childRight.box_.intersect(intersection.ray_)};

                    if (!traverseLeft && !traverseRight) {
                        ::std::advance(itStackBoxIndex, -1); // pop
                        boxIndex = *itStackBoxIndex;
                    } else {
                        boxIndex = (traverseLeft) ? left : right;
                        if (traverseLeft && traverseRight) {
                            *itStackBoxIndex = right;
                            ::std::advance(itStackBoxIndex, 1); // push
                        }
                    }
                }

            } else {
                ::std::advance(itStackBoxIndex, -1); // pop
                boxIndex = *itStackBoxIndex;
            }

        } while (itStackBoxIndex > itBeginBoxIndex);
        return intersection;
    }

    /**
     * Gets the index to where the vector of boxes should be split.
     * <br>
     * The algorithm used is the Surface Area Heuristic.
     *
     * @tparam Iterator The type of the iterator of the AABBs.
     * @param itBegin   The iterator of the first box in the vector.
     * @param itEnd     The iterator of the last box in the vector.
     * @return The index where the vector of boxes should be split.
     */
    template<typename T>
    template<typename Iterator>
    ::std::int32_t BVH<T>::getSplitIndexSah(const Iterator itBegin, const Iterator itEnd) {
        const long numberBoxes {static_cast<long>(itEnd - itBegin)};
        const Iterator itBoxes {itBegin};
        const long numBoxes {numberBoxes - 1};
        const ::std::uint32_t sizeUnsigned {static_cast<::std::uint32_t> (numBoxes)};

        ::std::vector<float> leftArea (sizeUnsigned);
        AABB leftBox {*itBoxes};
        const ::std::vector<float>::iterator itLeftArea {leftArea.begin()};
        *itLeftArea = leftBox.getSurfaceArea();
        for (::std::int32_t i {1}; i < numBoxes; ++i) {
            leftBox = surroundingBox(leftBox, *(itBoxes + i));
            *(itLeftArea + i) = leftBox.getSurfaceArea();
        }

        ::std::vector<float> rightArea (sizeUnsigned);
        AABB rightBox {*(itBoxes + numBoxes)};
        const ::std::vector<float>::iterator itRightArea {rightArea.begin()};
        *(itRightArea + numBoxes - 1) = rightBox.getSurfaceArea();
        for (long i {numBoxes - 2}; i >= 0; --i) {
            rightBox = surroundingBox(rightBox, *(itBoxes + i + 1));
            *(itRightArea + i) = rightBox.getSurfaceArea();
        }

        ::std::int32_t splitIndex {1};
        float minSah {*(itLeftArea) + numBoxes * *(itRightArea)};
        for (::std::int32_t i {1}; i < numBoxes; ++i) {
            const ::std::int32_t nextSplit {i + 1};
            const long numBoxesLeft {nextSplit};
            const long numBoxesRight {numberBoxes - numBoxesLeft};
            const float areaLeft {*(itLeftArea + i)};
            const float areaRight {*(itRightArea + i)};
            const float leftSah {numBoxesLeft * areaLeft};
            const float rightSah {numBoxesRight * areaRight};
            const float sah {leftSah + rightSah};
            if (sah < minSah) {
                splitIndex = nextSplit;
                minSah = sah;
            }
        }
        return splitIndex;
    }

    /**
     * Calculates a surrounding box of all the build nodes vector received via arguments.
     *
     * @tparam Iterator The type of the iterator of the BuildNodes.
     * @param itBegin The iterator of the first node in the vector.
     * @param itEnd   The iterator of the last node in the vector.
     * @return A box surrounding all the boxes of the nodes.
     */
    template<typename T>
    template<typename Iterator>
    AABB BVH<T>::getSurroundingBox(const Iterator itBegin, const Iterator itEnd) {
        AABB maxBox {itBegin->box_.getPointMin(), itBegin->box_.getPointMax()};

        for (Iterator it {itBegin + 1}; it < itEnd; ::std::advance(it, 1)) {
            const AABB &box {it->box_};
            maxBox = surroundingBox(maxBox, box);
        }

        return maxBox;
    }

    /**
     * Gets the primitives.
     *
     * @tparam T The type of the primitives.
     * @return The primitives.
     */
    template<typename T>
    const ::std::vector<T>& BVH<T>::getPrimitives() const {
        return this->primitives_;
    }


}//namespace MobileRT

#endif //MOBILERT_ACCELERATORS_BVH_HPP
