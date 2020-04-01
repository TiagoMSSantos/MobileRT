#ifndef MOBILERT_ACCELERATORS_REGULARGRID_HPP
#define MOBILERT_ACCELERATORS_REGULARGRID_HPP

#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Scene.hpp"
#include <glm/glm.hpp>
#include <mutex>
#include <omp.h>
#include <vector>

namespace MobileRT {

    /**
     * A class which represents the Regular Grid acceleration structure.
     * <br>
     * This is a structure where all the primitives are stored in a matrix where each cell represents a 3D space of
     * the scene.
     * So the scene geometry is divided in many boxes (each cell of the 3D matrix) and when a ray is casted into the
     * scene, it tries to intersect the nearest cell and then the primitives inside it until it finds the nearest
     * intersection point.
     *
     * @tparam T The type of the primitives.
     */
    template<typename T>
    class RegularGrid final {
    private:
        ::std::vector<::std::vector<T*>> grid_;
        ::std::vector<T> primitives_;
        ::std::int32_t gridSize_ {};
        ::std::uint32_t gridShift_ {};
        AABB worldBoundaries_ {};
        ::glm::vec3 cellSizeInverted_ {};
        ::glm::vec3 cellSize_ {};

    private:
        void addPrimitives();

        Intersection intersect(Intersection intersection, const Ray &ray, bool shadowTrace = false);

        ::std::uint32_t bitCounter(::std::uint32_t value) const;

        ::std::int32_t getCellIndex(::std::int32_t cellX, ::std::int32_t cellY, ::std::int32_t cellZ) const;

    public:
        explicit RegularGrid() = default;

        explicit RegularGrid(::std::vector<T> &&primitives,::std::uint32_t gridSize);

        RegularGrid(const RegularGrid &regularGrid) = delete;

        RegularGrid(RegularGrid &&regularGrid) noexcept = default;

        ~RegularGrid();

        RegularGrid &operator=(const RegularGrid &regularGrid) = delete;

        RegularGrid &operator=(RegularGrid &&regularGrid) noexcept = default;

        Intersection trace(Intersection intersection, const Ray &ray);

        Intersection shadowTrace(Intersection intersection, const Ray &ray);

        const ::std::vector<T>& getPrimitives() const;
    };



    /**
     * The constructor.
     *
     * @tparam T The type of the primitives.
     * @param primitives The primitives in the scene.
     * @param gridSize   The size of the cells in all the axes.
     */
    template<typename T>
    RegularGrid<T>::RegularGrid(
        ::std::vector<T> &&primitives, const ::std::uint32_t gridSize
    ) :
        grid_ {
            ::std::vector<::std::vector<T*>> {
                static_cast<::std::uint32_t> (gridSize * gridSize * gridSize)
            }
        },
        primitives_ {::std::move(primitives)},
        gridSize_ {static_cast<::std::int32_t> (gridSize)},
        gridShift_ {bitCounter(gridSize - 1U)},
        worldBoundaries_ {Scene::getBounds<T> (primitives_)},
        // precalculate 1 / size of a cell (for x, y and z)
        cellSizeInverted_ {
            gridSize_ / (worldBoundaries_.getPointMax() - worldBoundaries_.getPointMin())[0],
            gridSize_ / (worldBoundaries_.getPointMax() - worldBoundaries_.getPointMin())[1],
            gridSize_ / (worldBoundaries_.getPointMax() - worldBoundaries_.getPointMin())[2]
        },
        // precalculate size of a cell (for x, y, and z)
        cellSize_ {(worldBoundaries_.getPointMax() - worldBoundaries_.getPointMin()) * (1.0F / gridSize_)} {
        const auto worldBoundsMin {this->worldBoundaries_.getPointMin()};
        const auto worldBoundsMax {this->worldBoundaries_.getPointMax()};
        LOG("scene min=(",
            worldBoundsMin[0], ", ",
            worldBoundsMin[1], ", ",
            worldBoundsMin[2], ") max=(",
            worldBoundsMax[0], ", ",
            worldBoundsMax[1], ", ",
            worldBoundsMax[2], ")"
        );

        LOG("PRIMITIVES = ", this->primitives_.size());
        addPrimitives();
    }

    /**
     * The destructor.
     *
     * @tparam T The type of the primitives.
     */
    template<typename T>
    RegularGrid<T>::~RegularGrid() {
        this->grid_.clear();
        ::std::vector<::std::vector<T*>> {}.swap(this->grid_);
    }

    /**
     * Helper method which calculates the number bits set in an unsigned integer.
     *
     * @tparam T The type of the primitives.
     * @param value The value to count the number of the bits set.
     * @return The number of bits set in the value.
     */
    template<typename T>
    ::std::uint32_t RegularGrid<T>::bitCounter(::std::uint32_t value) const {
        ::std::uint32_t counter {};
        while (value > 0) {
            ++counter;
            value >>= 1;
        }
        return counter;
    }

    /**
     * Helper method which adds the primitives into the grid.
     *
     * @tparam T The type of the primitives.
     */
    template<typename T>
    void RegularGrid<T>::addPrimitives() {
        const auto worldBoundsMin {this->worldBoundaries_.getPointMin()};
        const auto worldBoundsMax {this->worldBoundaries_.getPointMax()};

        // calculate cell width, height and depth
        const auto size {worldBoundsMax - worldBoundsMin};
        const auto dx {size[0] / this->gridSize_};
        const auto dy {size[1] / this->gridSize_};
        const auto dz {size[2] / this->gridSize_};
        const auto dxReci {dx > 0 ? 1.0F / dx : 1.0F};
        const auto dyReci {dy > 0 ? 1.0F / dy : 1.0F};
        const auto dzReci {dz > 0 ? 1.0F / dz : 1.0F};
        const auto numPrimitives {static_cast<::std::uint32_t> (this->primitives_.size())};
        ::std::vector<::std::mutex> mutexes (this->grid_.size());
        const auto num_max_threads {omp_get_max_threads()};
        LOG("num_max_threads = ", num_max_threads);

        #pragma omp parallel for
        // store primitives in the grid cells
        for (::std::uint32_t index = 0; index < numPrimitives; ++index) {
            auto &primitive {this->primitives_[index]};
            const auto bound {primitive.getAABB()};
            const auto &bv1 {bound.getPointMin()};
            const auto &bv2 {bound.getPointMax()};

            // find out which cells could contain the primitive (based on aabb)
            auto x1 {static_cast<::std::int32_t> ((bv1[0] - worldBoundsMin[0]) * dxReci)};
            auto x2 {static_cast<::std::int32_t> ((bv2[0] - worldBoundsMin[0]) * dxReci) + 1};
            x1 = ::std::max(0, x1);
            x2 = ::std::min(x2, this->gridSize_ - 1);
            x2 = ::std::fabs(size[0]) < ::std::numeric_limits<float>::epsilon()? 0 : x2;
            x1 = ::std::min(x1, x2);
            auto y1 {static_cast<::std::int32_t> ((bv1[1] - worldBoundsMin[1]) * dyReci)};
            auto y2 {static_cast<::std::int32_t> ((bv2[1] - worldBoundsMin[1]) * dyReci) + 1};
            y1 = ::std::max(0, y1);
            y2 = ::std::min(y2, this->gridSize_ - 1);
            y2 = ::std::fabs(size[1]) < ::std::numeric_limits<float>::epsilon()? 0 : y2;
            y1 = ::std::min(y1, y2);
            auto z1 {static_cast<::std::int32_t> ((bv1[2] - worldBoundsMin[2]) * dzReci)};
            auto z2 {static_cast<::std::int32_t> ((bv2[2] - worldBoundsMin[2]) * dzReci) + 1};
            z1 = ::std::max(0, z1);
            z2 = ::std::min(z2, this->gridSize_ - 1);
            z2 = ::std::fabs(size[2]) < ::std::numeric_limits<float>::epsilon()? 0 : z2;
            z1 = ::std::min(z1, z2);

            //loop over candidate cells
            for (auto x {x1}; x <= x2; ++x) {
                for (auto y {y1}; y <= y2; ++y) {
                    for (auto z {z1}; z <= z2; ++z) {
                        // construct aabb for current cell
                        const auto idx {static_cast<::std::uint32_t> (
                            x +
                            y * this->gridSize_ +
                            z * this->gridSize_ * this->gridSize_
                        )};
                        const ::glm::vec3 &pos {
                                worldBoundsMin[0] + x * dx,
                                worldBoundsMin[1] + y * dy,
                                worldBoundsMin[2] + z * dz
                        };
                        const AABB &cell {pos, pos + ::glm::vec3 {dx, dy, dz}};
                        //LOG("min=(", pos[0], ", ", pos[1], ", ", pos[2], ") max=(", dx, ", ", dy, ",", dz, ")");
                        // do an accurate aabb / primitive intersection test
                        const auto intersectedBox {primitive.intersect(cell)};
                        if (intersectedBox) {
                            ::std::lock_guard<::std::mutex> lock {mutexes[idx]};
                            this->grid_[idx].emplace_back(&primitive);
//                            LOG("add idx = ", idx, " index = ", index);
                        }
                    }
                }
            }
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
    Intersection RegularGrid<T>::trace(Intersection intersection, const Ray &ray) {
        intersection = intersect (intersection, ray);
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
    Intersection RegularGrid<T>::shadowTrace(Intersection intersection, const Ray &ray) {
        intersection = intersect (intersection, ray, true);
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
     * @param ray          The casted ray.
     * @param shadowTrace  Whether it shouldn't find the nearest intersection point.
     * @return The intersection point of the ray in the scene.
     */
    template<typename T>
    Intersection RegularGrid<T>::intersect(Intersection intersection, const Ray &ray, const bool shadowTrace) {
        const auto worldBoundsMin {this->worldBoundaries_.getPointMin()};

        // setup 3DDDA (double check reusability of primary ray data)
        const auto &cell {(ray.origin_ - worldBoundsMin) * this->cellSizeInverted_};
        auto cellX {static_cast<::std::int32_t> (cell[0])};
        auto cellY {static_cast<::std::int32_t> (cell[1])};
        auto cellZ {static_cast<::std::int32_t> (cell[2])};

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
            cb[0] = (worldBoundsMin[0] + (cellX + 1) * this->cellSize_[0]);
        } else {
            stepX = -1;
            outX = -1;
            cb[0] = (worldBoundsMin[0] + cellX * this->cellSize_[0]);
        }

        if (ray.direction_[1] > 0) {
            stepY = 1;
            outY = this->gridSize_;
            cb[1] = (worldBoundsMin[1] + (cellY + 1) * this->cellSize_[1]);
        } else {
            stepY = -1;
            outY = -1;
            cb[1] = (worldBoundsMin[1] + cellY * this->cellSize_[1]);
        }

        if (ray.direction_[2] > 0) {
            stepZ = 1;
            outZ = this->gridSize_;
            cb[2] = (worldBoundsMin[2] + (cellZ + 1) * this->cellSize_[2]);
        } else {
            stepZ = -1;
            outZ = -1;
            cb[2] = (worldBoundsMin[2] + cellZ * this->cellSize_[2]);
        }

        ::glm::vec3 tmax {}, tdelta {};
        if (::std::fabs(ray.direction_[0]) > ::std::numeric_limits<float>::epsilon()) {
            const auto rxr {1.0F / ray.direction_[0]};
            tmax[0] = ((cb[0] - ray.origin_[0]) * rxr);
            tdelta[0] = (this->cellSize_[0] * stepX * rxr);
        } else {
            tmax[0] = RayLengthMax;
        }

        if (::std::fabs(ray.direction_[1]) > ::std::numeric_limits<float>::epsilon()) {
            const auto ryr {1.0F / ray.direction_[1]};
            tmax[1] = ((cb[1] - ray.origin_[1]) * ryr);
            tdelta[1] = (this->cellSize_[1] * stepY * ryr);
        } else {
            tmax[1] = RayLengthMax;
        }

        if (::std::fabs(ray.direction_[2]) > ::std::numeric_limits<float>::epsilon()) {
            const auto rzr {1.0F / ray.direction_[2]};
            tmax[2] = ((cb[2] - ray.origin_[2]) * rzr);
            tdelta[2] = (this->cellSize_[2] * stepZ * rzr);
        } else {
            tmax[2] = RayLengthMax;
        }

        // start stepping
        // trace primary ray
        while (true) {
            const auto index {getCellIndex(cellX, cellY, cellZ)};
            const auto itPrimitive {this->grid_.begin() + index};
            auto primitivesList {*itPrimitive};
            for (auto *const primitive : primitivesList) {
                const auto lastDist {intersection.length_};
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
            const auto index {getCellIndex(cellX, cellY, cellZ)};
            const auto itPrimitives {this->grid_.begin() + index};
            auto primitivesList {*itPrimitives};
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

    /**
     * Helper method which calculates the cell index in the grid.
     *
     * @param cellX The index of X.
     * @param cellY The index of Y.
     * @param cellZ The index of Z.
     * @return The index of the cell in the grid.
     */
    template<typename T>
    ::std::int32_t RegularGrid<T>::getCellIndex(
            const ::std::int32_t cellX,
            const ::std::int32_t cellY,
            const ::std::int32_t cellZ) const {
        const auto index {
            static_cast<::std::int32_t> (
                 static_cast<::std::uint32_t> (cellX) +
                 (static_cast<::std::uint32_t> (cellY) << (this->gridShift_)) +
                 (static_cast<::std::uint32_t> (cellZ) << (this->gridShift_ * 2U))
            )
        };
        return index;
    }

    /**
     * Gets the primitives.
     *
     * @tparam T The type of the primitives.
     * @return The primitives.
     */
    template<typename T>
    const ::std::vector<T>& RegularGrid<T>::getPrimitives() const {
        return this->primitives_;
    }

}//namespace MobileRT

#endif //MOBILERT_ACCELERATORS_REGULARGRID_HPP
