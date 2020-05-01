#include "MobileRT/Accelerators/AABB.hpp"
#include "MobileRT/Utils.hpp"

using ::MobileRT::AABB;
using ::MobileRT::Ray;

/**
 * Constructor.
 *
 * @param pointMin The point of the AABB in the bottom corner.
 * @param pointMax The point of the AABB in the upper corner.
 */
AABB::AABB(const ::glm::vec3 &pointMin, const ::glm::vec3 &pointMax) :
    pointMin_ {pointMin},
    pointMax_ {pointMax} {
    checkArguments();
}

/**
 * Helper method which checks for invalid fields.
 */
void AABB::checkArguments() const {
    ASSERT(isValid(this->pointMin_), "pointMin must be valid.");
    ASSERT(isValid(this->pointMax_), "pointMax must be valid.");
    ASSERT(!equal(this->pointMax_ - this->pointMin_, ::glm::vec3 {0}), "length can't be zero.");
}

/**
 * Checks if a ray intersects this AABB.
 *
 * @param ray A casted ray.
 * @return Whether the ray intersected this AABB.
 */
bool AABB::intersect(const Ray &ray) const {
    const auto invDirX {1.0F / ray.direction_[0]};
    const auto rayOrgX {ray.origin_[0]};
    const auto t1X {(this->pointMin_[0] - rayOrgX) * invDirX};
    const auto t2X {(this->pointMax_[0] - rayOrgX) * invDirX};
    auto tMin {::std::min(t1X, t2X)};
    auto tMax {::std::max(t1X, t2X)};

    for (auto axis {1}; axis < 3; ++axis) {
        const auto invDir {1.0F / ray.direction_[axis]};
        const auto rayOrg {ray.origin_[axis]};
        const auto t1 {(this->pointMin_[axis] - rayOrg) * invDir};
        const auto t2 {(this->pointMax_[axis] - rayOrg) * invDir};

        tMin = ::std::max(tMin, ::std::min(t1, t2));
        tMax = ::std::min(tMax, ::std::max(t1, t2));
    }

    const auto intersected {tMax >= ::std::max(tMin, 0.0F)};
    return intersected;
}

/**
 * Calculates the surface area of this AABB.
 *
 * @return The surface area.
 */
float AABB::getSurfaceArea() const {
    const auto length {this->pointMax_ - this->pointMin_};

    const auto bottomTopArea {2 * length[0] * length[2]};
    const auto sideAreaXY {2 * length[0] * length[1]};
    const auto sideAreaZY {2 * length[2] * length[1]};

    const auto surfaceArea {bottomTopArea + sideAreaXY + sideAreaZY};

    return surfaceArea;
}

/**
 * Calculates the centroid of this AABB.
 * <br>
 * The centroid or geometric center of a plane figure is the arithmetic mean position of all the points in the figure.
 *
 * @return The centroid.
 */
::glm::vec3 AABB::getCentroid() const {
    const auto &length {(this->pointMax_ - this->pointMin_) / 2.0F};
    const auto &res {this->pointMin_ + length};
    return res;
}

/**
 * Gets the point min of this AABB.
 *
 * @return The point min.
 */
::glm::vec3 AABB::getPointMin() const {
    return this->pointMin_;
}

/**
 * Gets the point max of this AABB.
 *
 * @return The point max.
 */
::glm::vec3 AABB::getPointMax() const {
    return this->pointMax_;
}

namespace MobileRT {
    /**
     * Calculates a box which surrounds both boxes received by the parameters.
     *
     * @param box1 A box.
     * @param box2 A box.
     * @return A box which surrounds both boxes.
     */
    AABB surroundingBox(const AABB &box1, const AABB &box2) {
        const auto &min {::glm::min(box1.getPointMin(), box2.getPointMin())};
        const auto &max {::glm::max(box1.getPointMax(), box2.getPointMax())};
        const AABB res {min, max};

        return res;
    }
}//namespace MobileRT
