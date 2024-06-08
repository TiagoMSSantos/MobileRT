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

        /**
         * The grid structure.
         */
        ::std::vector<::std::vector<T*>> grid_;

        /**
         * The primitives.
         */
        ::std::vector<T> primitives_;

        /**
         * The size of the cells in all the axes.
         */
        ::std::int32_t gridSize_ {};

        /**
         * The number of bits in the grid size value (minus 1).
         */
        ::std::uint32_t gridShift_ {};

        /**
         * The World boundaries.
         */
        AABB worldBoundaries_ {};

        /**
         * The inverted cell size (gridSize / worldBoundaries).
         */
        ::glm::vec3 cellSizeInverted_ {};

        /**
         * The size of a cell (worldBoundaries / gridSize).
         */
        ::glm::vec3 cellSize_ {};

    private:
        void addPrimitives();

        Intersection intersect(Intersection intersection);

        ::std::uint32_t bitCounter(::std::uint32_t value) const;

        ::std::int32_t getCellIndex(::std::int32_t cellX, ::std::int32_t cellY, ::std::int32_t cellZ) const;

    public:
        explicit RegularGrid() = default;

        explicit RegularGrid(::std::vector<T> &&primitives, ::std::uint32_t gridSize);

        RegularGrid(const RegularGrid &regularGrid) = delete;

        RegularGrid(RegularGrid &&regularGrid) noexcept = default;

        ~RegularGrid();

        RegularGrid &operator=(const RegularGrid &regularGrid) = delete;

        RegularGrid &operator=(RegularGrid &&regularGrid) noexcept = default;

        Intersection trace(Intersection intersection);

        Intersection shadowTrace(Intersection intersection);

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
            static_cast<float> (gridSize_) / (worldBoundaries_.getPointMax() - worldBoundaries_.getPointMin())[0],
            static_cast<float> (gridSize_) / (worldBoundaries_.getPointMax() - worldBoundaries_.getPointMin())[1],
            static_cast<float> (gridSize_) / (worldBoundaries_.getPointMax() - worldBoundaries_.getPointMin())[2]
        },
        // precalculate size of a cell (for x, y, and z)
        cellSize_ {(worldBoundaries_.getPointMax() - worldBoundaries_.getPointMin()) * (1.0F / static_cast<float> (gridSize_))} {
        ::MobileRT::checkSystemError("RegularGrid constructor start");
        LOG_INFO("Building RegularGrid for: ", typeid(T).name());
        const ::glm::vec3 worldBoundsMin {this->worldBoundaries_.getPointMin()};
        const ::glm::vec3 worldBoundsMax {this->worldBoundaries_.getPointMax()};
        LOG_INFO("scene min=(",
                  worldBoundsMin[0], ", ",
                  worldBoundsMin[1], ", ",
                  worldBoundsMin[2], ") max=(",
                  worldBoundsMax[0], ", ",
                  worldBoundsMax[1], ", ",
                  worldBoundsMax[2], ")"
        );

        LOG_INFO("PRIMITIVES = ", this->primitives_.size());

        ::MobileRT::checkSystemError("RegularGrid constructor before adding primitives");
        addPrimitives();

        ::MobileRT::checkSystemError("RegularGrid constructor end");
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
        LOG_INFO("Will add primitives to RegularGrid (", typeid(T).name(), ")");
        ::MobileRT::checkSystemError("RegularGrid addPrimitives start");
        const ::glm::vec3 worldBoundsMin {this->worldBoundaries_.getPointMin()};
        const ::glm::vec3 worldBoundsMax {this->worldBoundaries_.getPointMax()};

        // calculate cell width, height and depth
        const ::glm::vec3 size {worldBoundsMax - worldBoundsMin};
        const float dx {size[0] / this->gridSize_};
        const float dy {size[1] / this->gridSize_};
        const float dz {size[2] / this->gridSize_};
        const float dxReci {dx > 0 ? 1.0F / dx : 1.0F};
        const float dyReci {dy > 0 ? 1.0F / dy : 1.0F};
        const float dzReci {dz > 0 ? 1.0F / dz : 1.0F};
        const ::std::uint32_t numPrimitives {static_cast<::std::uint32_t> (this->primitives_.size())};

        ::std::vector<::std::mutex> mutexes (this->grid_.size());

        ::MobileRT::checkSystemError("RegularGrid addPrimitives before calling OpenMP");
        errno = 0; // In some compilers, OpenMP sets 'errno' to 'EFAULT - Bad address (14)'.
        // omp_get_max_threads(): Fatal signal 8 (SIGFPE) at 0xb7707ac8 (code=1), thread 3810 (pool-20-thread-)

        ::MobileRT::checkSystemError("RegularGrid addPrimitives before adding primitives");
        LOG_INFO("Adding primitives to RegularGrid (", typeid(T).name(), ") (mutexes: ", mutexes.size(), ")");
        // store primitives in the grid cells
        #pragma omp parallel for shared(mutexes)
        for (::std::int32_t index = 0; index < static_cast<::std::int32_t> (numPrimitives); ++index) {
            LOG_DEBUG("Adding primitive ", index, " to RegularGrid (", typeid(T).name(), ")");
            ::MobileRT::checkSystemError(::std::string("RegularGrid addPrimitives (" + ::std::to_string(index) + ")").c_str());
            T &primitive {this->primitives_[static_cast<::std::uint32_t> (index)]};
            const AABB bound {primitive.getAABB()};
            const ::glm::vec3 &bv1 {bound.getPointMin()};
            const ::glm::vec3 &bv2 {bound.getPointMax()};

            // find out which cells could contain the primitive (based on aabb)
            ::std::int32_t x1 {static_cast<::std::int32_t> ((bv1[0] - worldBoundsMin[0]) * dxReci)};
            ::std::int32_t x2 {static_cast<::std::int32_t> ((bv2[0] - worldBoundsMin[0]) * dxReci) + 1};
            x1 = ::std::max(0, x1);
            x2 = ::std::min(x2, this->gridSize_ - 1);
            x2 = ::std::fabs(size[0]) < ::std::numeric_limits<float>::epsilon()? 0 : x2;
            x1 = ::std::min(x1, x2);
            ::std::int32_t y1 {static_cast<::std::int32_t> ((bv1[1] - worldBoundsMin[1]) * dyReci)};
            ::std::int32_t y2 {static_cast<::std::int32_t> ((bv2[1] - worldBoundsMin[1]) * dyReci) + 1};
            y1 = ::std::max(0, y1);
            y2 = ::std::min(y2, this->gridSize_ - 1);
            y2 = ::std::fabs(size[1]) < ::std::numeric_limits<float>::epsilon()? 0 : y2;
            y1 = ::std::min(y1, y2);
            ::std::int32_t z1 {static_cast<::std::int32_t> ((bv1[2] - worldBoundsMin[2]) * dzReci)};
            ::std::int32_t z2 {static_cast<::std::int32_t> ((bv2[2] - worldBoundsMin[2]) * dzReci) + 1};
            z1 = ::std::max(0, z1);
            z2 = ::std::min(z2, this->gridSize_ - 1);
            z2 = ::std::fabs(size[2]) < ::std::numeric_limits<float>::epsilon()? 0 : z2;
            z1 = ::std::min(z1, z2);

            LOG_DEBUG("Looping over candidate cells for primitive ", index, " to RegularGrid (", typeid(T).name(), ") on coordinates: (", x1, "-", x2, ", ", y1, "-", y2, ", ", z1, "-", z2, ")");
            for (::std::int32_t x {x1}; x <= x2; ++x) {
                for (::std::int32_t y {y1}; y <= y2; ++y) {
                    for (::std::int32_t z {z1}; z <= z2; ++z) {
                        // construct aabb for current cell
                        const ::std::uint32_t idx {static_cast<::std::uint32_t> (
                            x +
                            y * this->gridSize_ +
                            z * this->gridSize_ * this->gridSize_
                        )};
                        const ::glm::vec3 &pos {
                            worldBoundsMin[0] + static_cast<float>(x) * dx,
                            worldBoundsMin[1] + static_cast<float>(y) * dy,
                            worldBoundsMin[2] + static_cast<float>(z) * dz
                        };
                        const AABB cell {pos, pos + ::glm::vec3 {dx, dy, dz}};
                        // do an accurate aabb / primitive intersection test
                        const bool intersectedBox {primitive.intersect(cell)};
                        if (intersectedBox) {
                            LOG_DEBUG("Adding primitive ", index, " (", idx, ") to RegularGrid (", typeid(T).name(), ") on coordinates: (", x, ", ", y, ", ", z, ")");
                            ::std::lock_guard<::std::mutex> lock {mutexes[idx]};
                            LOG_DEBUG("Acquired lock to add primitive ", index, " (", idx, ") to RegularGrid (", typeid(T).name(), ") on coordinates: (", x, ", ", y, ", ", z, ")");
                            this->grid_[idx].emplace_back(&primitive);
                            LOG_DEBUG("Added primitive ", index, " to RegularGrid (", typeid(T).name(), ") on coordinates: (", x, ", ", y, ", ", z, ")");
                        }
                    }
                }
            }
            LOG_DEBUG("Added primitive ", index, " to RegularGrid (", typeid(T).name(), ")");
            ::MobileRT::checkSystemError(::std::string("RegularGrid addPrimitives end (" + ::std::to_string(index) + ")").c_str());
        }
        LOG_INFO("Added primitives to RegularGrid (", typeid(T).name(), ")");
        ::MobileRT::checkSystemError("RegularGrid addPrimitives end");
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
    Intersection RegularGrid<T>::trace(Intersection intersection) {
        intersection = intersect (intersection);
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
    Intersection RegularGrid<T>::shadowTrace(Intersection intersection) {
        intersection = intersect (intersection);
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
    Intersection RegularGrid<T>::intersect(Intersection intersection) {
        const ::glm::vec3 &worldBoundsMin {this->worldBoundaries_.getPointMin()};

        // setup 3DDDA (double check reusability of primary ray data)
        const ::glm::vec3 &cell {(intersection.ray_.origin_ - worldBoundsMin) * this->cellSizeInverted_};
        ::std::int32_t cellX {static_cast<::std::int32_t> (cell[0])};
        ::std::int32_t cellY {static_cast<::std::int32_t> (cell[1])};
        ::std::int32_t cellZ {static_cast<::std::int32_t> (cell[2])};

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
        if (intersection.ray_.direction_[0] > 0) {
            stepX = 1;
            outX = this->gridSize_;
            cb[0] = (worldBoundsMin[0] + (static_cast<float> (cellX) + 1.0F) * this->cellSize_[0]);
        } else {
            stepX = -1;
            outX = -1;
            cb[0] = (worldBoundsMin[0] + static_cast<float> (cellX) * this->cellSize_[0]);
        }

        if (intersection.ray_.direction_[1] > 0) {
            stepY = 1;
            outY = this->gridSize_;
            cb[1] = (worldBoundsMin[1] + (static_cast<float> (cellY) + 1.0F) * this->cellSize_[1]);
        } else {
            stepY = -1;
            outY = -1;
            cb[1] = (worldBoundsMin[1] + static_cast<float> (cellY) * this->cellSize_[1]);
        }

        if (intersection.ray_.direction_[2] > 0) {
            stepZ = 1;
            outZ = this->gridSize_;
            cb[2] = (worldBoundsMin[2] + (static_cast<float> (cellZ) + 1.0F) * this->cellSize_[2]);
        } else {
            stepZ = -1;
            outZ = -1;
            cb[2] = (worldBoundsMin[2] + static_cast<float> (cellZ) * this->cellSize_[2]);
        }

        ::glm::vec3 tmax {}, tdelta {};
        if (::std::fabs(intersection.ray_.direction_[0]) > ::std::numeric_limits<float>::epsilon()) {
            const float rxr {1.0F / intersection.ray_.direction_[0]};
            tmax[0] = ((cb[0] - intersection.ray_.origin_[0]) * rxr);
            tdelta[0] = (this->cellSize_[0] * static_cast<float> (stepX) * rxr);
        } else {
            tmax[0] = RayLengthMax;
        }

        if (::std::fabs(intersection.ray_.direction_[1]) > ::std::numeric_limits<float>::epsilon()) {
            const float ryr {1.0F / intersection.ray_.direction_[1]};
            tmax[1] = ((cb[1] - intersection.ray_.origin_[1]) * ryr);
            tdelta[1] = (this->cellSize_[1] * static_cast<float> (stepY) * ryr);
        } else {
            tmax[1] = RayLengthMax;
        }

        if (::std::fabs(intersection.ray_.direction_[2]) > ::std::numeric_limits<float>::epsilon()) {
            const float rzr {1.0F / intersection.ray_.direction_[2]};
            tmax[2] = ((cb[2] - intersection.ray_.origin_[2]) * rzr);
            tdelta[2] = (this->cellSize_[2] * static_cast<float> (stepZ) * rzr);
        } else {
            tmax[2] = RayLengthMax;
        }

        // start stepping
        // trace primary ray
        while (true) {

            // Get the primitives inside the cell.
            const ::std::int32_t index {getCellIndex(cellX, cellY, cellZ)};
            const auto itPrimitives {this->grid_.begin() + index};
            ::std::vector<T*> primitivesList {*itPrimitives};

            // Check if the ray intersects any primitive in the cell.
            for (T *const primitive : primitivesList) {
                const float lastDist {intersection.length_};
                intersection = primitive->intersect(intersection);
                if (intersection.length_ < lastDist) {
                    if (intersection.ray_.shadowTrace_) {
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

            // Get the primitives in the cell.
            const ::std::int32_t index {getCellIndex(cellX, cellY, cellZ)};
            const auto itPrimitives {this->grid_.begin() + index};
            ::std::vector<T*> primitivesList {*itPrimitives};

            // Check if the ray intersects any primitive in the cell.
            for (T *const primitive : primitivesList) {
                intersection = primitive->intersect(intersection);
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
        const ::std::int32_t index {
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
