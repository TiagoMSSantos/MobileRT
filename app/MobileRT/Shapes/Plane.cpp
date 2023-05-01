#include "MobileRT/Shapes/Plane.hpp"

using ::MobileRT::AABB;
using ::MobileRT::Plane;
using ::MobileRT::Intersection;

/**
 * The constructor.
 *
 * @param point         A point in the plane.
 * @param normal        The normal of the plane.
 * @param materialIndex The index of the material of the plane.
 */
Plane::Plane(const ::glm::vec3 &point, const ::glm::vec3 &normal, const ::std::int32_t materialIndex) :
        normal_ {::glm::normalize(normal)},
        point_ {point},
        materialIndex_ {materialIndex} {
    checkArguments();
}

/**
 * Helper method which checks for invalid fields.
 */
void Plane::checkArguments() const {
    ASSERT(isValid(this->normal_), "normal must be valid.");
    ASSERT(!equal(this->normal_, ::glm::vec3 {0}), "normal can't be zero.");
    ASSERT(equal(::glm::length(this->normal_), 1.0F), "normal length must be 1.");

    ASSERT(isValid(this->point_), "point must be valid.");
}

/**
 * Determines if a ray intersects this plane or not and calculates the intersection point.
 *
 * @param intersection The previous intersection of the ray in the scene.
 * @return The intersection point.
 */
Intersection Plane::intersect(Intersection intersection) const {
    if (intersection.ray_.primitive_ == this) {
        return intersection;
    }

    // is ray parallel or contained in the Plane ??
    // planes have two sides!!!
    const auto normalizedProjection {::glm::dot(this->normal_, intersection.ray_.direction_)};
    if (::std::abs(normalizedProjection) < Epsilon) {
        return intersection;
    }

    //https://en.wikipedia.org/wiki/Line%E2%80%93plane_intersection
    const auto vecToPlane {this->point_ - intersection.ray_.origin_};
    const auto scalarProjectionVecToPlaneOnNormal {::glm::dot(this->normal_, vecToPlane)};
    const auto distanceToIntersection {scalarProjectionVecToPlaneOnNormal / normalizedProjection};

    // is it in front of the eye?
    // is it farther than the ray length ??
    if (distanceToIntersection < Epsilon || distanceToIntersection >= intersection.length_) {
        return intersection;
    }

    // if so, then we have an intersection
    const auto intersectionPoint {intersection.ray_.origin_ + intersection.ray_.direction_ * distanceToIntersection};
    const Intersection res {::std::move(intersection.ray_),
                            intersectionPoint,
                            distanceToIntersection,
                            this->normal_,
                            this,
                            this->materialIndex_
    };

    return res;
}

/**
 * The helper method which calculates the right vector.
 *
 * @return The right vector.
 */
::glm::vec3 Plane::getRightVector() const {
    ::glm::vec3 right {};
    if (this->normal_[0] >= 1) {
        right = ::glm::vec3 {0, 1, 1};
    } else if (this->normal_[1] >= 1) {
        right = ::glm::vec3 {1, 0, 1};
    } else if (this->normal_[2] >= 1) {
        right = ::glm::vec3 {1, 1, 0};
    } else if (this->normal_[0] <= -1) {
        right = ::glm::vec3 {0, 1, 1};
    } else if (this->normal_[1] <= -1) {
        right = ::glm::vec3 {1, 0, 1};
    } else if (this->normal_[2] <= -1) {
        right = ::glm::vec3 {1, 1, 0};
    }
    right = ::glm::normalize(right);
    return right;
}

/**
 * Calculates the bounding box of the plane.
 *
 * @return The bounding box of the plane.
 */
AABB Plane::getAABB() const {
    const auto &rightDir {getRightVector()};
    const auto &min {this->point_ + rightDir * -100.0F};
    const auto &max {this->point_ + rightDir * 100.0F};
    const AABB res {min, max};
    return res;
}

/**
 * Calculates the distance between a point and the plane.
 *
 * @param point A 3D point in the scene.
 * @return The distance between the point and the plane.
 */
float Plane::distance(const ::glm::vec3 &point) const {
    //Plane Equation
    //a(x-x0)+b(y-y0)+c(z-z0) = 0
    //abc = normal
    //x0,y0,z0 = point
    //D = |ax0 + by0 + cz0 + d| / sqrt(a² + b² + c²)
    const auto d {
        this->normal_[0] * -this->point_[0] +
        this->normal_[1] * -this->point_[1] +
        this->normal_[2] * -this->point_[2]
    };
    const auto numerator {this->normal_[0] * point[0] + this->normal_[1] * point[1] + this->normal_[2] * point[2] + d};
    const auto denumerator {
        ::std::sqrt(
            this->normal_[0] * this->normal_[0] +
            this->normal_[1] * this->normal_[1] +
            this->normal_[2] * this->normal_[2]
        )
    };
    const auto res {numerator / denumerator};
    return res;
}

/**
 * Checks if a bounding box intersects the plane or not.
 *
 * @param box A bounding box.
 * @return Whether if the bounding box intersects the plane or not.
 */
bool Plane::intersect(const AABB &box) const {
    const auto &positiveVertex {box.getPointMax()};
    const auto &negativeVertex {box.getPointMin()};

    const auto distanceP {distance(positiveVertex)};
    const auto distanceN {distance(negativeVertex)};
    const auto res {(distanceP <= 0 && distanceN >= 0) || (distanceP >= 0 && distanceN <= 0)};

    return res;
}

/**
 * Gets the normal of this plane.
 *
 * @return The normal.
 */
::glm::vec3 Plane::getNormal () const {
    return this->normal_;
}

/**
 * Gets the point of this plane.
 *
 * @return The point.
 */
::glm::vec3 Plane::getPoint () const {
    return this->point_;
}

/**
 * Gets the material index of this plane.
 *
 * @return The material index.
 */
::std::int32_t Plane::getMaterialIndex () const {
    return this->materialIndex_;
}
