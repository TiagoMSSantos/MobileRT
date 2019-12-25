#ifndef MOBILERT_ACCELERATORS_REGULARGRID_HPP
#define MOBILERT_ACCELERATORS_REGULARGRID_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Primitive.hpp"
#include "MobileRT/Scene.hpp"
#include <glm/glm.hpp>
#include <vector>

namespace MobileRT {

    template<typename T>
    class RegularGrid final {
    private:
        ::std::vector<::std::vector<::MobileRT::Primitive <T> *>> primitives_;
        ::std::int32_t gridSize_ {};
        ::std::int32_t gridShift_ {};
        AABB box_ {};
        ::glm::vec3 cellSizeInverted_ {};
        ::glm::vec3 cellSize_ {};

    private:
        template<typename P>
        void addPrimitives(::std::vector<P> &&primitives) noexcept;

        template<typename P>
        Intersection intersect(Intersection intersection,const Ray &ray, bool shadowTrace = false) noexcept;

        ::std::int32_t bitCounter(::std::uint32_t value) const noexcept;

    public:
        explicit RegularGrid() noexcept = default;

        explicit RegularGrid(
                AABB sceneBounds,
                ::std::int32_t gridSize,
                ::std::vector<::MobileRT::Primitive<T>> &&primitives
        ) noexcept;

        RegularGrid(const RegularGrid &regularGrid) noexcept = delete;

        RegularGrid(RegularGrid &&regularGrid) noexcept = default;

        ~RegularGrid() noexcept;

        RegularGrid &operator=(const RegularGrid &regularGrid) noexcept = delete;

        RegularGrid &operator=(RegularGrid &&regularGrid) noexcept = default;

        Intersection trace(Intersection intersection, const Ray &ray) noexcept;

        Intersection shadowTrace(Intersection intersection, const Ray &ray) noexcept;
    };



    template<typename T>
    RegularGrid<T>::RegularGrid(
        AABB sceneBounds,
        const ::std::int32_t gridSize,
        ::std::vector<::MobileRT::Primitive<T>> &&primitives
    ) noexcept :
        primitives_ {
            ::std::vector<::std::vector<::MobileRT::Primitive<T>*>> {
                static_cast<::std::size_t> (gridSize * gridSize * gridSize)}
        },
        gridSize_ {gridSize},
        gridShift_ {bitCounter(static_cast<::std::uint32_t>(gridSize)) - 1},
        box_(sceneBounds),//world boundaries
        // precalculate 1 / size of a cell (for x, y and z)
        cellSizeInverted_ {gridSize_ / (box_.pointMax_ - box_.pointMin_)[0],
              gridSize_ / (box_.pointMax_ - box_.pointMin_)[1],
              gridSize_ / (box_.pointMax_ - box_.pointMin_)[2]},
        // precalculate size of a cell (for x, y, and z)
        cellSize_ {(box_.pointMax_ - box_.pointMin_) * (1.0F / gridSize_)} {
        LOG("scene min=(",
            this->box_.pointMin_[0], ", ",
            this->box_.pointMin_[1], ", ",
            this->box_.pointMin_[2], ") max=(",
            this->box_.pointMax_[0], ", ",
            this->box_.pointMax_[1], ", ",
            this->box_.pointMax_[2], ")"
        );

        const auto vectorSize {static_cast<::std::size_t>(gridSize * gridSize * gridSize)};
        this->primitives_.reserve(vectorSize);

        addPrimitives<Primitive<T>>(::std::move(primitives));
        LOG("PRIMITIVES = ", this->primitives_.size());
    }

    template<typename T>
    RegularGrid<T>::~RegularGrid() noexcept {
        this->primitives_.clear();
        ::std::vector<::std::vector<Primitive<T> *>> {}.swap(this->primitives_);
    }

    template<typename T>
    ::std::int32_t RegularGrid<T>::bitCounter(::std::uint32_t value) const noexcept {
        ::std::int32_t counter {};
        while (value > 0) {
            ++counter;
            value >>= 1;
        }
        return counter;
    }

    template<typename T>
    template<typename P>
    void RegularGrid<T>::addPrimitives(::std::vector<P> &&primitives) noexcept {
        ::std::int32_t index {};

        // calculate cell width, height and depth
        const float sizeX {this->box_.pointMax_[0] - this->box_.pointMin_[0]};
        const float sizeY {this->box_.pointMax_[1] - this->box_.pointMin_[1]};
        const float sizeZ {this->box_.pointMax_[2] - this->box_.pointMin_[2]};
        const float dx {sizeX / this->gridSize_};
        const float dy {sizeY / this->gridSize_};
        const float dz {sizeZ / this->gridSize_};
        const float dxReci {dx > 0 ? 1.0F / dx : 1.0F};
        const float dyReci {dy > 0 ? 1.0F / dy : 1.0F};
        const float dzReci {dz > 0 ? 1.0F / dz : 1.0F};

        // store primitives in the grid cells
        for (auto &primitive : primitives) {
            ++index;
            const AABB bound {primitive.getAABB()};
            const ::glm::vec3 &bv1 {bound.pointMin_};
            const ::glm::vec3 &bv2 {bound.pointMax_};

            // find out which cells could contain the primitive (based on aabb)
            auto x1 {static_cast<::std::int32_t>((bv1[0] - this->box_.pointMin_[0]) * dxReci)};
            auto x2 {static_cast<::std::int32_t>((bv2[0] - this->box_.pointMin_[0]) * dxReci) + 1};
            x1 = ::std::max(0, x1);
            x2 = ::std::min(x2, this->gridSize_ - 1);
            x2 = ::std::fabs(sizeX) < ::std::numeric_limits<float>::epsilon()? 0 : x2;
            x1 = ::std::min(x1, x2);
            auto y1 {static_cast<::std::int32_t>((bv1[1] - this->box_.pointMin_[1]) * dyReci)};
            auto y2 {static_cast<::std::int32_t>((bv2[1] - this->box_.pointMin_[1]) * dyReci) + 1};
            y1 = ::std::max(0, y1);
            y2 = ::std::min(y2, this->gridSize_ - 1);
            y2 = ::std::fabs(sizeY) < ::std::numeric_limits<float>::epsilon()? 0 : y2;
            y1 = ::std::min(y1, y2);
            auto z1 {static_cast<::std::int32_t>((bv1[2] - this->box_.pointMin_[2]) * dzReci)};
            auto z2 {static_cast<::std::int32_t>((bv2[2] - this->box_.pointMin_[2]) * dzReci) + 1};
            z1 = ::std::max(0, z1);
            z2 = ::std::min(z2, this->gridSize_ - 1);
            z2 = ::std::fabs(sizeZ) < ::std::numeric_limits<float>::epsilon()? 0 : z2;
            z1 = ::std::min(z1, z2);

            //loop over candidate cells
            for (::std::int32_t x {x1}; x <= x2; ++x) {
                for (::std::int32_t y {y1}; y <= y2; ++y) {
                    for (::std::int32_t z {z1}; z <= z2; ++z) {
                        // construct aabb for current cell
                        const auto idx {static_cast<::std::size_t>(
                            x +
                            y * this->gridSize_ +
                            z * this->gridSize_ * this->gridSize_
                        )};
                        const ::glm::vec3 &pos {
                            this->box_.pointMin_[0] + x * dx,
                            this->box_.pointMin_[1] + y * dy,
                            this->box_.pointMin_[2] + z * dz
                        };
                        const AABB &cell {pos, pos + ::glm::vec3 {dx, dy, dz}};
                        //LOG("min=(", pos[0], ", ", pos[1], ", ", pos[2], ") max=(", dx, ", ", dy, ",", dz, ")");
                        // do an accurate aabb / primitive intersection test
                        const bool intersectedBox {::MobileRT::intersect(primitive, cell)};
                        if (intersectedBox) {
                            this->primitives_[idx].emplace_back(&primitive);
                            //LOG("add idx = ", idx, " index = ", index);
                        }
                    }
                }
            }
        }
    }

    template<typename T>
    Intersection RegularGrid<T>::trace(Intersection intersection, const Ray &ray) noexcept {
        intersection = intersect<::MobileRT::Primitive<T>>(intersection, ray);
        return intersection;
    }

    template<typename T>
    Intersection RegularGrid<T>::shadowTrace(Intersection intersection, const Ray &ray) noexcept {
        intersection = intersect<::MobileRT::Primitive<T>>(intersection, ray, true);
        return intersection;
    }

    template<typename T>
    template<typename P>
    Intersection RegularGrid<T>::intersect(Intersection intersection, const Ray &ray, const bool shadowTrace) noexcept {
        // setup 3DDDA (double check reusability of primary ray data)
        const ::glm::vec3 &cell {(ray.origin_ - box_.pointMin_) * this->cellSizeInverted_};
        auto cellX {static_cast<::std::int32_t>(cell[0])};
        auto cellY {static_cast<::std::int32_t>(cell[1])};
        auto cellZ {static_cast<::std::int32_t>(cell[2])};

        cellX = ::std::min(cellX, this->gridSize_ - 1);
        cellX = ::std::max(cellX, 0);
        cellY = ::std::min(cellY, this->gridSize_ - 1);
        cellY = ::std::max(cellY, 0);
        cellZ = ::std::min(cellZ, this->gridSize_ - 1);
        cellZ = ::std::max(cellZ, 0);

        ::std::int32_t stepX {}, outX {};
        ::std::int32_t stepY {}, outY {};
        ::std::int32_t stepZ {}, outZ {};
        ::glm::vec3 cb {};
        if (ray.direction_[0] > 0) {
            stepX = 1;
            outX = this->gridSize_;
            cb[0] = (this->box_.pointMin_[0] + (cellX + 1) * this->cellSize_[0]);
        } else {
            stepX = -1;
            outX = -1;
            cb[0] = (this->box_.pointMin_[0] + cellX * this->cellSize_[0]);
        }

        if (ray.direction_[1] > 0) {
            stepY = 1;
            outY = this->gridSize_;
            cb[1] = (this->box_.pointMin_[1] + (cellY + 1) * this->cellSize_[1]);
        } else {
            stepY = -1;
            outY = -1;
            cb[1] = (this->box_.pointMin_[1] + cellY * this->cellSize_[1]);
        }

        if (ray.direction_[2] > 0) {
            stepZ = 1;
            outZ = this->gridSize_;
            cb[2] = (this->box_.pointMin_[2] + (cellZ + 1) * this->cellSize_[2]);
        } else {
            stepZ = -1;
            outZ = -1;
            cb[2] = (this->box_.pointMin_[2] + cellZ * this->cellSize_[2]);
        }

        ::glm::vec3 tmax {}, tdelta {};
        if (::std::fabs(ray.direction_[0]) > ::std::numeric_limits<float>::epsilon()) {
            const float rxr {1.0F / ray.direction_[0]};
            tmax[0] = ((cb[0] - ray.origin_[0]) * rxr);
            tdelta[0] = (this->cellSize_[0] * stepX * rxr);
        } else {
            tmax[0] = RayLengthMax;
        }

        if (::std::fabs(ray.direction_[1]) > ::std::numeric_limits<float>::epsilon()) {
            const float ryr {1.0F / ray.direction_[1]};
            tmax[1] = ((cb[1] - ray.origin_[1]) * ryr);
            tdelta[1] = (this->cellSize_[1] * stepY * ryr);
        } else {
            tmax[1] = RayLengthMax;
        }

        if (::std::fabs(ray.direction_[2]) > ::std::numeric_limits<float>::epsilon()) {
            const float rzr {1.0F / ray.direction_[2]};
            tmax[2] = ((cb[2] - ray.origin_[2]) * rzr);
            tdelta[2] = (this->cellSize_[2] * stepZ * rzr);
        } else {
            tmax[2] = RayLengthMax;
        }

        // start stepping
        // trace primary ray
        while (true) {
            const auto index {
                static_cast<::std::int32_t>(
                     static_cast<::std::uint32_t> (cellX) +
                    (static_cast<::std::uint32_t> (cellY) << (static_cast<::std::uint32_t> (this->gridShift_))) +
                    (static_cast<::std::uint32_t> (cellZ) << (static_cast<::std::uint32_t> (this->gridShift_) * 2u))
                )
            };
            const auto itPrimitive {this->primitives_.begin() + index};
            ::std::vector<P *> primitivesList {*itPrimitive};
            for (auto *const primitive : primitivesList) {
                const float lastDist {intersection.length_};
                intersection = primitive->intersect(intersection, ray);
                if (intersection.length_ < lastDist) {
                    if (shadowTrace) {
                        return intersection;
                    }
                    goto testloop;
                }
            }

            if (tmax[0] < tmax[1]) {
                if (tmax[0] < tmax[2]) {
                    cellX += stepX;
                    if (cellX == outX) {
                        return intersection;
                    }
                    tmax[0] = (tmax[0] + tdelta[0]);
                } else {
                    cellZ += stepZ;
                    if (cellZ == outZ) {
                        return intersection;
                    }
                    tmax[2] = (tmax[2] + tdelta[2]);
                }
            } else {
                if (tmax[1] < tmax[2]) {
                    cellY += stepY;
                    if (cellY == outY) {
                        return intersection;
                    }
                    tmax[1] = (tmax[1] + tdelta[1]);
                } else {
                    cellZ += stepZ;
                    if (cellZ == outZ) {
                        return intersection;
                    }
                    tmax[2] = (tmax[2] + tdelta[2]);
                }
            }

        }
        testloop:
        while (true) {
            const auto index {
                static_cast<::std::int32_t> (
                     static_cast<::std::uint32_t> (cellX) +
                    (static_cast<::std::uint32_t> (cellY) << (static_cast<::std::uint32_t> (this->gridShift_))) +
                    (static_cast<::std::uint32_t> (cellZ) << (static_cast<::std::uint32_t> (this->gridShift_) * 2u))
                )
            };
            const auto itPrimitives {this->primitives_.begin() + index};
            ::std::vector<P *> primitivesList {*itPrimitives};
            for (auto *const primitive : primitivesList) {
                intersection = primitive->intersect(intersection, ray);
            }
            if (tmax[0] < tmax[1]) {
                if (tmax[0] < tmax[2]) {
                    if (intersection.length_ < tmax[0]) {
                        break;
                    }
                    cellX += stepX;
                    if (cellX == outX) {
                        break;
                    }
                    tmax[0] = (tmax[0] + tdelta[0]);
                } else {
                    if (intersection.length_ < tmax[2]) {
                        break;
                    }
                    cellZ += stepZ;
                    if (cellZ == outZ) {
                        break;
                    }
                    tmax[2] = (tmax[2] + tdelta[2]);
                }
            } else {
                if (tmax[1] < tmax[2]) {
                    if (intersection.length_ < tmax[1]) {
                        break;
                    }
                    cellY += stepY;
                    if (cellY == outY) {
                        break;
                    }
                    tmax[1] = (tmax[1] + tdelta[1]);
                } else {
                    if (intersection.length_ < tmax[2]) {
                        break;
                    }
                    cellZ += stepZ;
                    if (cellZ == outZ) {
                        break;
                    }
                    tmax[2] = (tmax[2] + tdelta[2]);
                }
            }
        }
        return intersection;
    }
}//namespace MobileRT

#endif //MOBILERT_ACCELERATORS_REGULARGRID_HPP
