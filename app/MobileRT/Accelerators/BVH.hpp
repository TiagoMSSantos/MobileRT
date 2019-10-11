//
// Created by puscas on 27/08/17.
//

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

    static const ::std::uint32_t maxLeafSize {2};

    struct BVHNode {
        AABB box_ {};
        ::std::uint32_t indexOffset_ {0};
        ::std::uint32_t numberPrimitives_ {0};
    };

    template<typename T, typename Iterator>
    ::std::uint32_t getSplitIndex_SAH (
        Iterator itBegin, Iterator itEnd) noexcept;

    template<typename T>
    class BVH final {
    private:
        ::std::vector<BVHNode> boxes_ {};

    public:
        ::std::vector<Primitive<T>> primitives_ {};

    private:
        void build() noexcept;

    public:
        explicit BVH() noexcept = default;

        explicit BVH<T>(
                ::std::vector<Primitive<T>> &&primitives) noexcept;

        BVH(const BVH &bVH) noexcept = delete;

        BVH(BVH &&bVH) noexcept = default;

        ~BVH() noexcept;

        BVH &operator=(const BVH &bVH) noexcept = delete;

        BVH &operator=(BVH &&bVH) noexcept = default;

        Intersection trace(
          Intersection intersection,
          const Ray &ray) noexcept;

        Intersection shadowTrace(
          Intersection intersection,
          const Ray &ray) noexcept;
    };



    template<typename T>
    BVH<T>::BVH(::std::vector<Primitive<T>> &&primitives) noexcept {
        if (primitives.empty()) {
            BVHNode bvhNode {};
            boxes_.emplace_back(bvhNode);
            return;
        }
        primitives_ = ::std::move(primitives);
        const ::std::uint32_t numberPrimitives {static_cast<::std::uint32_t>(primitives_.size())};
        const ::std::uint32_t maxNodes {numberPrimitives * 2 - 1};
        boxes_.resize(maxNodes);
        build();
    }

    template<typename T>
    BVH<T>::~BVH() noexcept {
        boxes_.clear();
        primitives_.clear();

        ::std::vector<BVHNode>{}.swap(boxes_);
        ::std::vector<Primitive < T>> {}.swap(primitives_);
    }

    template<typename T>
    void BVH<T>::build() noexcept {
        ::std::uint32_t id {0};
        ::std::uint32_t begin {0};
        ::std::uint32_t end {static_cast<::std::uint32_t>(primitives_.size())};
        ::std::uint32_t maxId {0};

        ::std::array<::std::uint32_t, 512> stackId {};
        ::std::array<::std::uint32_t, 512> stackBegin {};
        ::std::array<::std::uint32_t, 512> stackEnd {};

        auto itStackId {stackId.begin()};
        ::std::advance(itStackId, 1);

        auto itStackBegin {stackBegin.begin()};
        ::std::advance(itStackBegin, 1);

        auto itStackEnd {stackEnd.begin()};
        ::std::advance(itStackEnd, 1);

        const auto itBoxes {boxes_.begin()};
        const auto itPrimitives {primitives_.begin()};
        do {
            (itBoxes + static_cast<::std::int32_t> (id))->box_ = (itPrimitives + static_cast<::std::int32_t> (begin))->getAABB();
            ::std::vector<AABB> boxes {(itBoxes + static_cast<::std::int32_t> (id))->box_};
            const ::std::uint32_t boxPrimitivesSize {end - begin};
            boxes.reserve(boxPrimitivesSize);
            for (::std::uint32_t i {begin + 1}; i < end; ++i) {
                const AABB &new_box {(itPrimitives + static_cast<::std::int32_t> (i))->getAABB()};
                (itBoxes + static_cast<::std::int32_t> (id))->box_ = surroundingBox(new_box, (itBoxes + static_cast<::std::int32_t> (id))->box_);
                boxes.emplace_back(new_box);
            }

            if (boxPrimitivesSize <= maxLeafSize) {
                (itBoxes + static_cast<::std::int32_t> (id))->indexOffset_ = begin;
                (itBoxes + static_cast<::std::int32_t> (id))->numberPrimitives_ = boxPrimitivesSize;

                ::std::advance(itStackId, -1); // pop
                id = *itStackId;
                ::std::advance(itStackBegin, -1); // pop
                begin = *itStackBegin;
                ::std::advance(itStackEnd, -1); // pop
                end = *itStackEnd;
            } else {
                const ::std::uint32_t left {maxId + 1};
                (itBoxes + static_cast<::std::int32_t> (id))->indexOffset_ = left;
                maxId = left + 1 > maxId? left + 1 : maxId;

                const ::std::uint32_t splitIndex {boxPrimitivesSize <= 2*maxLeafSize? 2 :
                    static_cast<::std::uint32_t>(getSplitIndex_SAH<T>(boxes.begin(), boxes.end()))
                };

                *itStackId = left + 1;
                ::std::advance(itStackId, 1); // push
                *itStackBegin = begin + splitIndex;
                ::std::advance(itStackBegin, 1); // push
                *itStackEnd = end;
                ::std::advance(itStackEnd, 1); // push

                id = left;
                end = begin + splitIndex;
            }
        } while(itStackId > stackId.begin());
        LOG("maxNodeId = ", maxId);
        boxes_.erase (boxes_.begin() + static_cast<::std::int32_t>(maxId) + 1, boxes_.end());
        boxes_.shrink_to_fit();
        ::std::vector<BVHNode>{boxes_}.swap(boxes_);
    }

    template<typename T>
    Intersection BVH<T>::trace(
            Intersection intersection,
            const Ray &ray) noexcept {
        if(primitives_.empty()) {
            return intersection;
        }
        ::std::uint32_t id {0};
        ::std::array<::std::uint32_t, 512> stackId {};

        auto itStackId {stackId.begin()};
        ::std::advance(itStackId, 1);

        const auto itBoxes {boxes_.begin()};
        const auto itPrimitives {primitives_.begin()};
        do {
            const BVHNode &node {*(itBoxes + static_cast<::std::int32_t> (id))};
            if (intersect(node.box_, ray)) {

                const ::std::uint32_t numberPrimitives {node.numberPrimitives_};
                if (numberPrimitives > 0) {
                    for (::std::uint32_t i {0}; i < numberPrimitives; ++i) {
                        auto& primitive {*(itPrimitives + static_cast<::std::int32_t> (node.indexOffset_ + i))};
                        intersection = primitive.intersect(intersection, ray);
                    }
                    ::std::advance(itStackId, -1); // pop
                    id = *itStackId;
                } else {
                    const ::std::uint32_t left {node.indexOffset_};
                    const BVHNode &childL {*(itBoxes + static_cast<::std::int32_t> (left))};
                    const BVHNode &childR {*(itBoxes + static_cast<::std::int32_t> (left + 1))};

                    const bool traverseL {intersect(childL.box_, ray)};
                    const bool traverseR {intersect(childR.box_, ray)};

                    if (!traverseL && !traverseR) {
                        ::std::advance(itStackId, -1); // pop
                        id = *itStackId;
                    } else {
                        id = (traverseL) ? left : left + 1;
                        if (traverseL && traverseR) {
                            *itStackId = left + 1;
                            ::std::advance(itStackId, 1); // push
                        }
                    }
                }

            } else {
                ::std::advance(itStackId, -1); // pop
                id = *itStackId;
            }

        } while (itStackId > stackId.begin());
        return intersection;
    }

    template<typename T>
    Intersection BVH<T>::shadowTrace(
        Intersection intersection,
        const Ray &ray) noexcept {
        if(primitives_.empty()) {
            return intersection;
        }
        ::std::uint32_t id {0};
        ::std::array<::std::uint32_t, 512> stackId {};

        auto itStackId {stackId.begin()};
        ::std::advance(itStackId, 1);

        const auto itBoxes {boxes_.begin()};
        const auto itPrimitives {primitives_.begin()};
        do {
            const BVHNode &node {*(itBoxes + static_cast<::std::int32_t> (id))};
            if (intersect(node.box_, ray)) {

                const ::std::uint32_t numberPrimitives {node.numberPrimitives_};
                if (numberPrimitives > 0) {
                    for (::std::uint32_t i {0}; i < numberPrimitives; ++i) {
                        auto& primitive {*(itPrimitives + static_cast<::std::int32_t> (node.indexOffset_ + i))};
                        const float lastDist {intersection.length_};
                        intersection = primitive.intersect(intersection, ray);
                        if (intersection.length_ < lastDist) {
                            return intersection;
                        }
                    }
                    ::std::advance(itStackId, -1); // pop
                    id = *itStackId;
                } else {
                    const ::std::uint32_t left {node.indexOffset_};
                    const BVHNode &childL {*(itBoxes + static_cast<::std::int32_t> (left))};
                    const BVHNode &childR {*(itBoxes + static_cast<::std::int32_t> (left + 1))};

                    const bool traverseL {intersect(childL.box_, ray)};
                    const bool traverseR {intersect(childR.box_, ray)};

                    if (!traverseL && !traverseR) {
                        ::std::advance(itStackId, -1); // pop
                        id = *itStackId;
                    } else {
                        id = (traverseL) ? left : left + 1;
                        if (traverseL && traverseR) {
                            *itStackId = left + 1;
                            ::std::advance(itStackId, 1); // push
                        }
                    }
                }

            } else {
                ::std::advance(itStackId, -1); // pop
                id = *itStackId;
            }

        } while (itStackId > stackId.begin());
        return intersection;
    }

    template<typename T, typename Iterator>
    ::std::uint32_t getSplitIndex_SAH (
        const Iterator itBegin, const Iterator itEnd) noexcept {
            const ::std::uint32_t N {static_cast<::std::uint32_t>(itEnd - itBegin)};
            const auto itBoxes {itBegin};

            ::std::vector<float> left_area (N - maxLeafSize);
            AABB left_box {*itBoxes};
            const auto itLeftArea {left_area.begin()};
            *itLeftArea = left_box.getSurfaceArea();
            for (::std::uint32_t i {1}; i < N - maxLeafSize; ++i) {
                left_box = surroundingBox(left_box, *(itBoxes + static_cast<::std::int32_t> (i)));
                *(itLeftArea + static_cast<::std::int32_t> (i)) = left_box.getSurfaceArea();
            }

            ::std::vector<float> right_area (N - maxLeafSize);
            AABB right_box {*(itBoxes + static_cast<::std::int32_t> (N) - 1)};
            const auto itRightArea {right_area.begin()};
            *(itRightArea + static_cast<::std::int32_t> (N - maxLeafSize - 1)) = right_box.getSurfaceArea();
            for (::std::uint32_t i {N - 2}; i > maxLeafSize; --i) {
                right_box = surroundingBox(right_box, *(itBoxes + static_cast<::std::int32_t> (i)));
                *(itRightArea + static_cast<::std::int32_t> (i - maxLeafSize)) = right_box.getSurfaceArea();
            }

            ::std::uint32_t splitIndex {maxLeafSize};
            float min_SAH {
                maxLeafSize * *(itLeftArea + static_cast<::std::int32_t> (maxLeafSize - 1)) +
                (N - maxLeafSize) * *(itRightArea + static_cast<::std::int32_t> (maxLeafSize - 1))};
            for (::std::uint32_t i {maxLeafSize}; i < N - maxLeafSize; ++i) {
                const float SAH_left {(i + 1) * *(itLeftArea + static_cast<::std::int32_t> (i))};
                const float SAH_right {(N - (i + 1)) * *(itRightArea + static_cast<::std::int32_t> (i))};
                const float SAH {SAH_left + SAH_right};
                if (SAH < min_SAH) {
                    splitIndex = i + 1;
                    min_SAH = SAH;
                }
            }
            return splitIndex;
    }

}//namespace MobileRT

#endif //MOBILERT_ACCELERATORS_BVH_HPP
