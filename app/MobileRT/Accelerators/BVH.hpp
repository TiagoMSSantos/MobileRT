#ifndef MOBILERT_ACCELERATORS_BVH_HPP
#define MOBILERT_ACCELERATORS_BVH_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Intersection.hpp"
#include "MobileRT/Scene.hpp"
#include <algorithm>
#include <array>
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
            static const ::std::int32_t maxLeafSize {2};

            /**
             * An auxiliary node used for the construction of the BVH.
             * It is used to order all the AABBs by the position of the centroid.
             */
            struct BuildNode {
                AABB box_ {};
                ::glm::vec3 centroid_ {};
                ::std::int32_t oldIndex_ {};

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

        private:
            ::std::vector<BVHNode> boxes_ {};
            ::std::vector<T> primitives_ {};

        private:
            void build(::std::vector<T> &&primitives);

            Intersection intersect(Intersection intersection, const Ray &ray, bool shadowTrace = false);

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

            Intersection trace(Intersection intersection, const Ray &ray);

            Intersection shadowTrace(Intersection intersection, const Ray &ray);

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
            BVHNode bvhNode {};
            this->boxes_.emplace_back(bvhNode);
            return;
        }
        LOG("Building BVH");
        const auto numPrimitives {(primitives.size())};
        const auto maxNodes {numPrimitives * 2 - 1};
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
        const auto primitivesSize {primitives.size()};
        ::std::int32_t endBoxIndex {static_cast<::std::int32_t> (primitivesSize)};
        ::std::int32_t maxNodeIndex {};

        ::std::array<::std::int32_t, 512> stackBoxIndex {};
        ::std::array<::std::int32_t, 512> stackBoxBegin {};
        ::std::array<::std::int32_t, 512> stackBoxEnd {};

        auto itStackBoxIndex {stackBoxIndex.begin()};
        ::std::advance(itStackBoxIndex, 1);

        auto itStackBoxBegin {stackBoxBegin.begin()};
        ::std::advance(itStackBoxBegin, 1);

        auto itStackBoxEnd {stackBoxEnd.begin()};
        ::std::advance(itStackBoxEnd, 1);

        const auto itStackBoxIndexBegin {stackBoxIndex.cbegin()};

        ::std::vector<BuildNode> buildNodes {};
        buildNodes.reserve(primitivesSize);
        for (::std::uint32_t i {}; i < primitivesSize; ++i) {
            const auto &primitive {primitives [i]};
            auto &&box {primitive.getAABB()};
            BuildNode &&node {::std::move(box), static_cast<::std::int32_t> (i)};
            buildNodes.emplace_back(::std::move(node));
        }

        do {
            const auto &currentBox {this->boxes_.begin() + currentBoxIndex};
            const auto boxPrimitivesSize {endBoxIndex - beginBoxIndex};
            const auto itBegin {buildNodes.begin() + beginBoxIndex};

            const auto itEnd {buildNodes.begin() + endBoxIndex};
            const auto surroundingBox {getSurroundingBox(itBegin, itEnd)};
            const auto maxDist {surroundingBox.pointMax_ - surroundingBox.pointMin_};
            const auto maxAxis {
                    maxDist[0] >= maxDist[1] && maxDist[0] >= maxDist[2]
                    ? 0
                    : maxDist[1] >= maxDist[0] && maxDist[1] >= maxDist[2]
                      ? 1
                      : 2
            };

//            ::std::sort(itBegin, itEnd,
//                [&](const BuildNode &node1, const BuildNode &node2) {
//                    return node1.centroid_[maxAxis] < node2.centroid_[maxAxis];
//                }
//            );

            const auto numBuckets {10};
            const auto step {maxDist / static_cast<float> (numBuckets)};
            const auto stepAxis {step[maxAxis]};
            const auto &startBox {surroundingBox.pointMin_[maxAxis]};
            const auto bucket1 {startBox + stepAxis};
            auto itBucket {::std::partition(itBegin, itEnd,
                             [&](const BuildNode &node) {
                                 return node.centroid_[maxAxis] < bucket1;
                             }
            )};
            for (::std::int32_t i = 2; i < numBuckets; ++i) {
                const auto bucket {startBox + stepAxis * i};
                itBucket = ::std::partition(itBucket, itEnd,
                        [&](const BuildNode &node) {
                            return node.centroid_[maxAxis] < bucket;
                        }
                );
            }

            currentBox->box_ = itBegin->box_;
            ::std::vector<AABB> boxes {currentBox->box_};
            boxes.reserve(static_cast<::std::uint32_t> (boxPrimitivesSize));
            for (::std::int32_t i = beginBoxIndex + 1; i < endBoxIndex; ++i) {
                const AABB &newBox {buildNodes[static_cast<::std::uint32_t> (i)].box_};
                currentBox->box_ = ::MobileRT::surroundingBox(newBox, currentBox->box_);
                boxes.emplace_back(newBox);
            }

            if (boxPrimitivesSize <= maxLeafSize) {
                currentBox->indexOffset_ = beginBoxIndex;
                currentBox->numPrimitives_ = boxPrimitivesSize;

                ::std::advance(itStackBoxIndex, -1); // pop
                currentBoxIndex = *itStackBoxIndex;
                ::std::advance(itStackBoxBegin, -1); // pop
                beginBoxIndex = *itStackBoxBegin;
                ::std::advance(itStackBoxEnd, -1); // pop
                endBoxIndex = *itStackBoxEnd;
            } else {
                const auto left {maxNodeIndex + 1};
                const auto right {left + 1};
                const auto splitIndex {getSplitIndexSah(boxes.begin(), boxes.end())};

                currentBox->indexOffset_ = left;
                maxNodeIndex = ::std::max(right, maxNodeIndex);

                *itStackBoxIndex = right;
                ::std::advance(itStackBoxIndex, 1); // push
                *itStackBoxBegin = beginBoxIndex + splitIndex;
                ::std::advance(itStackBoxBegin, 1); // push
                *itStackBoxEnd = endBoxIndex;
                ::std::advance(itStackBoxEnd, 1); // push

                currentBoxIndex = left;
                endBoxIndex = beginBoxIndex + splitIndex;
            }
        } while(itStackBoxIndex > itStackBoxIndexBegin);

        LOG("maxNodeId = ", maxNodeIndex);
        this->boxes_.erase (this->boxes_.begin() + maxNodeIndex + 1, this->boxes_.end());
        this->boxes_.shrink_to_fit();
        ::std::vector<BVHNode> {this->boxes_}.swap(this->boxes_);

        this->primitives_.reserve(primitivesSize);
        for (::std::uint32_t i {}; i < primitivesSize; ++i) {
            const auto &node {buildNodes[i]};
            const auto oldIndex {static_cast<::std::uint32_t> (node.oldIndex_)};
            this->primitives_.emplace_back(::std::move(primitives[oldIndex]));
        }
    }

    /**
     * This method casts a ray into the geometry and calculates the nearest intersection point from the origin of the
     * ray.
     *
     * @tparam T The type of the primitives.
     * @param intersection The current intersection of the ray with previous primitives.
     * @param ray          The ray to be casted.
     * @return The intersection of the ray with the geometry.
     */
    template<typename T>
    Intersection BVH<T>::trace(Intersection intersection, const Ray &ray) {
        intersection = intersect(intersection, ray);
        return intersection;
    }

    /**
     * This method casts a ray into the geometry and calculates a random intersection point.
     * The intersection point itself is not important, the important is to determine if the ray intersects some
     * primitive in the scene or not.
     *
     * @tparam T The type of the primitives.
     * @param intersection The current intersection of the ray with previous primitives.
     * @param ray          The ray to be casted.
     * @return The intersection of the ray with the geometry.
     */
    template<typename T>
    Intersection BVH<T>::shadowTrace(Intersection intersection, const Ray &ray) {
        intersection = intersect(intersection, ray, true);
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
     * nearest intersection poin.
     * @param ray          The casted ray.
     * @param shadowTrace  Whether it shouldn't find the nearest intersection point.
     * @return The intersection point of the ray in the scene.
     */
    template<typename T>
    Intersection BVH<T>::intersect(Intersection intersection, const Ray &ray, const bool shadowTrace) {
        if(this->primitives_.empty()) {
            return intersection;
        }
        ::std::int32_t boxIndex {};
        ::std::array<::std::int32_t, 512> stackBoxIndex {};

        const auto beginBoxIndex {stackBoxIndex.cbegin()};
        auto itStackBoxIndex {stackBoxIndex.begin()};
        ::std::advance(itStackBoxIndex, 1);

        const auto itBoxes {this->boxes_.begin()};
        const auto itPrimitives {this->primitives_.begin()};
        do {
            const auto &node {*(itBoxes + boxIndex)};
            if (node.box_.intersect(ray)) {

                const auto numberPrimitives {node.numPrimitives_};
                if (numberPrimitives > 0) {
                    for (::std::int32_t i {}; i < numberPrimitives; ++i) {
                        auto& primitive {*(itPrimitives + node.indexOffset_ + i)};
                        const auto lastDist {intersection.length_};
                        intersection = primitive.intersect(intersection, ray);
                        if (shadowTrace && intersection.length_ < lastDist) {
                            return intersection;
                        }
                    }
                    ::std::advance(itStackBoxIndex, -1); // pop
                    boxIndex = *itStackBoxIndex;
                } else {
                    const auto left {node.indexOffset_};
                    const auto right {node.indexOffset_ + 1};
                    const auto &childLeft {*(itBoxes + left)};
                    const auto &childRight {*(itBoxes + right)};

                    const auto traverseLeft {childLeft.box_.intersect(ray)};
                    const auto traverseRight {childRight.box_.intersect(ray)};

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

        } while (itStackBoxIndex > beginBoxIndex);
        return intersection;
    }

    /**
     * Gets the index to where the vector of boxes should be split.
     *
     * @tparam Iterator The type of the iterator of the AABBs.
     * @param itBegin   The iterator of the first box in the vector.
     * @param itEnd     The iterator of the last box in the vector.
     * @return The index where the vector of boxes should be split.
     */
    template<typename T>
    template<typename Iterator>
    ::std::int32_t BVH<T>::getSplitIndexSah(const Iterator itBegin, const Iterator itEnd) {
        const auto numberBoxes {itEnd - itBegin};
        const auto itBoxes {itBegin};
        const auto numBoxes {numberBoxes - 1};
        const auto sizeUnsigned {static_cast<::std::uint32_t> (numBoxes)};

        ::std::vector<float> leftArea (sizeUnsigned);
        auto leftBox {*itBoxes};
        const auto itLeftArea {leftArea.begin()};
        *itLeftArea = leftBox.getSurfaceArea();
        for (auto i {1}; i < numBoxes; ++i) {
            leftBox = surroundingBox(leftBox, *(itBoxes + i));
            *(itLeftArea + i) = leftBox.getSurfaceArea();
        }

        ::std::vector<float> rightArea (sizeUnsigned);
        auto rightBox {*(itBoxes + numBoxes)};
        const auto itRightArea {rightArea.begin()};
        *(itRightArea + numBoxes - 1) = rightBox.getSurfaceArea();
        for (auto i {numBoxes - 2}; i >= 0; --i) {
            rightBox = surroundingBox(rightBox, *(itBoxes + i + 1));
            *(itRightArea + i) = rightBox.getSurfaceArea();
        }

        auto splitIndex {1};
        auto minSah {*(itLeftArea) + numBoxes * *(itRightArea)};
        for (auto i {1}; i < numBoxes; ++i) {
            const auto nextSplit {i + 1};
            const auto numBoxesLeft {nextSplit};
            const auto numBoxesRight {numberBoxes - numBoxesLeft};
            const auto areaLeft {*(itLeftArea + i)};
            const auto areaRight {*(itRightArea + i)};
            const auto leftSah {numBoxesLeft * areaLeft};
            const auto rightSah {numBoxesRight * areaRight};
            const auto sah {leftSah + rightSah};
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
        AABB maxBox {itBegin->box_.pointMin_, itBegin->box_.pointMax_};

        for (auto it {itBegin + 1}; it < itEnd; ::std::advance(it, 1)) {
            const auto &box {it->box_};
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
