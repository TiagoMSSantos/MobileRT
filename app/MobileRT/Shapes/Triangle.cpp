#include "MobileRT/Shapes/Triangle.hpp"
#include <cmath>

using ::MobileRT::AABB;
using ::MobileRT::Triangle;
using ::MobileRT::Intersection;

/**
 * The constructor.
 *
 * @param pointA        A vertex of the triangle.
 * @param pointB        A vertex of the triangle.
 * @param pointC        A vertex of the triangle.
 * @param materialIndex The index of the material of the triangle.
 */
Triangle::Triangle(
        const ::glm::vec3 &pointA,
        const ::glm::vec3 &pointB,
        const ::glm::vec3 &pointC,
        const ::std::int32_t materialIndex
) :
    AC_ {pointC - pointA},
    AB_ {pointB - pointA},
    pointA_ {pointA},
    normalA_ {::glm::normalize(::glm::cross(AC_, AB_))},
    normalB_ {::glm::normalize(::glm::cross(AC_, AB_))},
    normalC_ {::glm::normalize(::glm::cross(AC_, AB_))},
    materialIndex_ {materialIndex} {

    assert(!::glm::all(::glm::isnan(this->normalA_)));
    assert(!::glm::all(::glm::isnan(this->normalB_)));
    assert(!::glm::all(::glm::isnan(this->normalC_)));

    assert(!::glm::all(::glm::isinf(this->normalA_)));
    assert(!::glm::all(::glm::isinf(this->normalB_)));
    assert(!::glm::all(::glm::isinf(this->normalC_)));
}

/**
 * The constructor.
 *
 * @param pointA        A vertex of the triangle.
 * @param pointB        A vertex of the triangle.
 * @param pointC        A vertex of the triangle.
 * @param normalA       The normal of the first vertex.
 * @param normalB       The normal of the second vertex.
 * @param normalC       The normal of the third vertex.
 * @param materialIndex The index of the material of the triangle.
 */
Triangle::Triangle(
        const ::glm::vec3 &pointA,
        const ::glm::vec3 &pointB,
        const ::glm::vec3 &pointC,
        const ::glm::vec3 &normalA,
        const ::glm::vec3 &normalB,
        const ::glm::vec3 &normalC,
        const ::std::int32_t materialIndex
) :
    AC_ {pointC - pointA},
    AB_ {pointB - pointA},
    pointA_ {pointA},
    normalA_ {::glm::normalize(normalA)},
    normalB_ {::glm::normalize(normalB)},
    normalC_ {::glm::normalize(normalC)},
    materialIndex_ {materialIndex} {

    assert(!::glm::all(::glm::isnan(this->normalA_)));
    assert(!::glm::all(::glm::isnan(this->normalB_)));
    assert(!::glm::all(::glm::isnan(this->normalC_)));

    assert(!::glm::all(::glm::isinf(this->normalA_)));
    assert(!::glm::all(::glm::isinf(this->normalB_)));
    assert(!::glm::all(::glm::isinf(this->normalC_)));
}

/**
 * The constructor.
 *
 * @param pointA        A vertex of the triangle.
 * @param pointB        A vertex of the triangle.
 * @param pointC        A vertex of the triangle.
 * @param texCoordA     The texture coordinates of the first vertex.
 * @param texCoordB     The texture coordinates of the second vertex.
 * @param texCoordC     The texture coordinates of the third vertex.
 * @param materialIndex The index of the material of the triangle.
 */
Triangle::Triangle(
        const ::glm::vec3 &pointA,
        const ::glm::vec3 &pointB,
        const ::glm::vec3 &pointC,
        const ::glm::vec2 &texCoordA,
        const ::glm::vec2 &texCoordB,
        const ::glm::vec2 &texCoordC,
        const ::std::int32_t materialIndex
) :
    AC_ {pointC - pointA},
    AB_ {pointB - pointA},
    pointA_ {pointA},
    normalA_ {::glm::normalize(::glm::cross(AC_, AB_))},
    normalB_ {::glm::normalize(::glm::cross(AC_, AB_))},
    normalC_ {::glm::normalize(::glm::cross(AC_, AB_))},
    texCoordA_ {texCoordA},
    texCoordB_ {texCoordB},
    texCoordC_ {texCoordC},
    materialIndex_ {materialIndex} {

    assert(!::glm::all(::glm::isnan(this->normalA_)));
    assert(!::glm::all(::glm::isnan(this->normalB_)));
    assert(!::glm::all(::glm::isnan(this->normalC_)));

    assert(!::glm::all(::glm::isinf(this->normalA_)));
    assert(!::glm::all(::glm::isinf(this->normalB_)));
    assert(!::glm::all(::glm::isinf(this->normalC_)));
}

/**
 * The constructor.
 *
 * @param pointA        A vertex of the triangle.
 * @param pointB        A vertex of the triangle.
 * @param pointC        A vertex of the triangle.
 * @param normalA       The normal of the first vertex.
 * @param normalB       The normal of the second vertex.
 * @param normalC       The normal of the third vertex.
 * @param texCoordA     The texture coordinates of the first vertex.
 * @param texCoordB     The texture coordinates of the second vertex.
 * @param texCoordC     The texture coordinates of the third vertex.
 * @param materialIndex The index of the material of the triangle.
 */
Triangle::Triangle(
        const ::glm::vec3 &pointA,
        const ::glm::vec3 &pointB,
        const ::glm::vec3 &pointC,
        const ::glm::vec3 &normalA,
        const ::glm::vec3 &normalB,
        const ::glm::vec3 &normalC,
        const ::glm::vec2 &texCoordA,
        const ::glm::vec2 &texCoordB,
        const ::glm::vec2 &texCoordC,
        const ::std::int32_t materialIndex
) :
    AC_ {pointC - pointA},
    AB_ {pointB - pointA},
    pointA_ {pointA},
    normalA_ {normalA},
    normalB_ {normalB},
    normalC_ {normalC},
    texCoordA_ {texCoordA},
    texCoordB_ {texCoordB},
    texCoordC_ {texCoordC},
    materialIndex_ {materialIndex} {

    assert(!::glm::all(::glm::isnan(this->normalA_)));
    assert(!::glm::all(::glm::isnan(this->normalB_)));
    assert(!::glm::all(::glm::isnan(this->normalC_)));

    assert(!::glm::all(::glm::isinf(this->normalA_)));
    assert(!::glm::all(::glm::isinf(this->normalB_)));
    assert(!::glm::all(::glm::isinf(this->normalC_)));
}

/**
 * Determines if a ray intersects this triangle or not and calculates the intersection point.
 *
 * @param intersection The previous intersection of the ray in the scene.
 * @param ray          The casted ray into the scene.
 * @return The intersection point.
 */
Intersection Triangle::intersect(const Intersection &intersection, const Ray &ray) const {
    if (ray.primitive_ == this) {
        return intersection;
    }

    const auto &perpendicularVector {::glm::cross(ray.direction_, this->AC_)};
    const auto normalizedProjection {::glm::dot(this->AB_, perpendicularVector)};
    if (::std::abs(normalizedProjection) < Epsilon) {
        return intersection;
    }

    //u v = barycentric coordinates (uv-space are inside a unit triangle)
    const auto normalizedProjectionInv {1.0F / normalizedProjection};
    const auto &vectorToCamera {ray.origin_ - this->pointA_};
    const auto u {normalizedProjectionInv * ::glm::dot(vectorToCamera, perpendicularVector)};
    if (u < 0.0F || u > 1.0F) {
        return intersection;
    }

    const auto &upPerpendicularVector {::glm::cross(vectorToCamera, this->AB_)};
    const auto v {normalizedProjectionInv * ::glm::dot (ray.direction_, upPerpendicularVector)};
    if (v < 0.0F || (u + v) > 1.0F) {
        return intersection;
    }

    // at this stage we can compute t to find out where
    // the intersection point is on the line
    const auto distanceToIntersection {normalizedProjectionInv * ::glm::dot(AC_, upPerpendicularVector)};

    if (distanceToIntersection < Epsilon || distanceToIntersection >= intersection.length_) {
        return intersection;
    }

    const auto w {1.0F - u - v};
    const auto &intersectionNormal {::glm::normalize(this->normalA_ * w + this->normalB_ * u + this->normalC_ * v)};
    const auto &texCoords {this->texCoordA_ * w + this->texCoordB_ * u + this->texCoordC_ * v};
    const auto &intersectionPoint {ray.origin_ + ray.direction_ * distanceToIntersection};
    const Intersection res {intersectionPoint, distanceToIntersection, intersectionNormal, this,
                            this->materialIndex_, texCoords};

    return res;
}

/**
 * Calculates the bounding box of the triangle.
 *
 * @return The bounding box of the triangle.
 */
AABB Triangle::getAABB() const {
    const auto &pointB {this->pointA_ + this->AB_};
    const auto &pointC {this->pointA_ + this->AC_};
    const auto &min {::glm::min(this->pointA_, ::glm::min(pointB, pointC))};
    const auto &max {::glm::max(this->pointA_, ::glm::max(pointB, pointC))};
    const AABB &res {min, max};
    return res;
}

/**
 * Checks if a bounding box intersects the triangle or not.
 *
 * @param box A bounding box.
 * @return Whether if the bounding box intersects the triangle or not.
 */
bool Triangle::intersect(const AABB &box) const {
    auto intersectRayAABB {
        [&](const ::glm::vec3 &orig, const ::glm::vec3 &vec) -> bool {
            ::glm::vec3 t1 {};
            ::glm::vec3 t2 {}; // vectors to hold the T-values for every direction
            auto tNear {::std::numeric_limits<float>::min()};
            auto tFar {::std::numeric_limits<float>::max()};
            if (::std::fabs(vec[0]) < ::std::numeric_limits<float>::epsilon()) {
                // ray parallel to planes in this direction
                if ((orig[0] < box.pointMin_[0]) || ((orig[0] + vec[0]) > box.pointMax_[0])) {
                    return false; // parallel AND outside box : no intersection possible
                }
            } else { // ray not parallel to planes in this direction
                t1[0] = ((box.pointMin_[0] - orig[0]) / vec[0]);
                t2[0] = ((box.pointMax_[0] - orig[0]) / vec[0]);
                if (t1[0] > t2[0]) { // we want t1 to hold values for intersection with near plane
                    ::std::swap(t1, t2);
                }
                tNear = ::std::max(t1[0], tNear);
                tFar = ::std::min(t2[0], tFar);
                if ((tNear > tFar) || (tFar < 0)) {
                    return false;
                }
            }
            if (::std::fabs(vec[1]) < ::std::numeric_limits<float>::epsilon()) {
                // ray parallel to planes in this direction
                if ((orig[1] < box.pointMin_[1]) || ((orig[1] + vec[1]) > box.pointMax_[1])) {
                    return false; // parallel AND outside box : no intersection possible
                }
            } else { // ray not parallel to planes in this direction
                t1[1] = ((box.pointMin_[1] - orig[1]) / vec[1]);
                t2[1] = ((box.pointMax_[1] - orig[1]) / vec[1]);
                if (t1[1] > t2[1]) { // we want t1 to hold values for intersection with near plane
                    ::std::swap(t1, t2);
                }
                tNear = ::std::max(t1[1], tNear);
                tFar = ::std::min(t2[1], tFar);
                if ((tNear > tFar) || (tFar < 0)) {
                    return false;
                }
            }
            if (::std::fabs(vec[2]) < ::std::numeric_limits<float>::epsilon()) {
                // ray parallel to planes in this direction
                if ((orig[2] < box.pointMin_[2]) || ((orig[2] + vec[2]) > box.pointMax_[2])) {
                    return false; // parallel AND outside box : no intersection possible
                }
            } else { // ray not parallel to planes in this direction
                t1[2] = ((box.pointMin_[2] - orig[2]) / vec[2]);
                t2[2] = ((box.pointMax_[2] - orig[2]) / vec[2]);
                if (t1[2] > t2[2]) { // we want t1 to hold values for intersection with near plane
                    ::std::swap(t1, t2);
                }
                tNear = ::std::max(t1[2], tNear);
                tFar = ::std::min(t2[2], tFar);
                if ((tNear > tFar) || (tFar < 0)) {
                    return false;
                }
            }
            return true; // if we made it here, there was an intersection - YAY
        }};

    auto isOverTriangle {
        [&](const ::glm::vec3 &vec) -> bool {
            const auto &perpendicularVector {::glm::cross(vec, this->AC_)};
            const auto normalizedProjection {::glm::dot(this->AB_, perpendicularVector)};
            const auto res {::std::abs(normalizedProjection) < Epsilon};
            return res;
        }
    };

    const auto &min {box.pointMin_};
    const auto &max {box.pointMax_};
    const auto &vec {max - min};
    const Ray &ray {vec, min, 1};
    const auto intersectedAB {intersectRayAABB(this->pointA_, this->AB_)};
    const auto intersectedAC {intersectRayAABB(this->pointA_, this->AC_)};
    const auto &pointB {this->pointA_ + this->AB_};
    const auto &pointC {this->pointA_ + this->AC_};
    const auto intersectedBC {intersectRayAABB(pointB, pointC - pointB)};
    Intersection intersection {};
    const auto lastDist {intersection.length_};
    intersection = intersect(intersection, ray);
    const auto intersectedRay {intersection.length_ < lastDist};
    const auto insideTriangle {isOverTriangle(vec)};
    const auto res {intersectedAB || intersectedAC || intersectedBC || intersectedRay || insideTriangle};

    return res;
}
