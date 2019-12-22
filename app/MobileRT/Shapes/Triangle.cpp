#include "MobileRT/Shapes/Triangle.hpp"

using ::MobileRT::AABB;
using ::MobileRT::Triangle;
using ::MobileRT::Intersection;

Triangle::Triangle(
        const ::glm::vec3 &pointA, const ::glm::vec3 &pointB, const ::glm::vec3 &pointC) noexcept :
        AC_{pointC - pointA},
        AB_{pointB - pointA},
        pointA_{pointA}
{
}

Intersection Triangle::intersect(const Intersection &intersection, const Ray &ray) const noexcept {
    if (ray.primitive_ == this) {
        return intersection;
    }

    const ::glm::vec3 &perpendicularVector {::glm::cross(ray.direction_, AC_)};
    const float normalizedProjection {::glm::dot(AB_, perpendicularVector)};
    if (::std::abs(normalizedProjection) < Epsilon) {
        return intersection;
    }

    //u v = barycentric coordinates (uv-space are inside a unit triangle)
    const float normalizedProjectionInv {1.0F / normalizedProjection};
    const ::glm::vec3 &vectorToCamera {ray.origin_ - pointA_};
    const float u {normalizedProjectionInv * ::glm::dot(vectorToCamera, perpendicularVector)};
    if (u < 0.0F || u > 1.0F) {
        return intersection;
    }

    const ::glm::vec3 &upPerpendicularVector {::glm::cross(vectorToCamera, AB_)};
    const float v {normalizedProjectionInv * ::glm::dot (ray.direction_, upPerpendicularVector)};
    if (v < 0.0F || (u + v) > 1.0F) {
        return intersection;
    }

    // at this stage we can compute t to find out where
    // the intersection point is on the line
    const float distanceToIntersection {normalizedProjectionInv * ::glm::dot(AC_, upPerpendicularVector)};

    if (distanceToIntersection < Epsilon || distanceToIntersection >= intersection.length_) {
        return intersection;
    }
    const ::glm::vec3 &intersectionNormal1{::glm::normalize(::glm::cross(AB_, AC_))};
    const ::glm::vec3 &intersectionNormal2{::glm::normalize(::glm::cross(AC_, AB_))};
    const ::glm::vec3 &intersectionNormal{
            ::glm::dot(intersectionNormal1, ray.direction_) < 0.0F ? intersectionNormal1
                                                                   : intersectionNormal2};

    const ::glm::vec3 &intersectionPoint{ray.origin_ + ray.direction_ * distanceToIntersection};
    const Intersection res {intersectionPoint, distanceToIntersection, intersectionNormal, this};
    return res;
}

AABB Triangle::getAABB() const noexcept {
    const ::glm::vec3 &pointB {pointA_ + AB_};
    const ::glm::vec3 &pointC {pointA_ + AC_};
    const ::glm::vec3 &min {::glm::min(pointA_, ::glm::min(pointB, pointC))};
    const ::glm::vec3 &max {::glm::max(pointA_, ::glm::max(pointB, pointC))};
    const AABB res {min, max};
    return res;
}

bool Triangle::intersect(const AABB &box) const noexcept {
    auto intersectRayAABB {
        [&](const ::glm::vec3 &orig, const ::glm::vec3 &vec) noexcept -> bool {
            ::glm::vec3 T_1 {};
            ::glm::vec3 T_2 {}; // vectors to hold the T-values for every direction
            float t_near {::std::numeric_limits<float>::min()};
            float t_far {::std::numeric_limits<float>::max()};
            if (::std::fabs(vec[0]) < ::std::numeric_limits<float>::epsilon()) {
                // ray parallel to planes in this direction
                if ((orig[0] < box.pointMin_[0]) ||
                    ((orig[0] + vec[0]) > box.pointMax_[0])) {
                    return false; // parallel AND outside box : no intersection possible
                }
            } else { // ray not parallel to planes in this direction
                T_1[0] = ((box.pointMin_[0] - orig[0]) / vec[0]);
                T_2[0] = ((box.pointMax_[0] - orig[0]) / vec[0]);
                if (T_1[0] >
                    T_2[0]) { // we want T_1 to hold values for intersection with near plane
                    ::std::swap(T_1, T_2);
                }
                t_near = ::std::max(T_1[0], t_near);
                t_far = ::std::min(T_2[0], t_far);
                if ((t_near > t_far) || (t_far < 0)) {
                    return false;
                }
            }
            if (::std::fabs(vec[1]) < ::std::numeric_limits<float>::epsilon()) {
                // ray parallel to planes in this direction
                if ((orig[1] < box.pointMin_[1]) ||
                    ((orig[1] + vec[1]) > box.pointMax_[1])) {
                    return false; // parallel AND outside box : no intersection possible
                }
            } else { // ray not parallel to planes in this direction
                T_1[1] = ((box.pointMin_[1] - orig[1]) / vec[1]);
                T_2[1] = ((box.pointMax_[1] - orig[1]) / vec[1]);
                if (T_1[1] >
                    T_2[1]) { // we want T_1 to hold values for intersection with near plane
                    ::std::swap(T_1, T_2);
                }
                t_near = ::std::max(T_1[1], t_near);
                t_far = ::std::min(T_2[1], t_far);
                if ((t_near > t_far) || (t_far < 0)) {
                    return false;
                }
            }
            if (::std::fabs(vec[2]) < ::std::numeric_limits<float>::epsilon()) {
                // ray parallel to planes in this direction
                if ((orig[2] < box.pointMin_[2]) ||
                    ((orig[2] + vec[2]) > box.pointMax_[2])) {
                    return false; // parallel AND outside box : no intersection possible
                }
            } else { // ray not parallel to planes in this direction
                T_1[2] = ((box.pointMin_[2] - orig[2]) / vec[2]);
                T_2[2] = ((box.pointMax_[2] - orig[2]) / vec[2]);
                if (T_1[2] >
                    T_2[2]) { // we want T_1 to hold values for intersection with near plane
                    ::std::swap(T_1, T_2);
                }
                t_near = ::std::max(T_1[2], t_near);
                t_far = ::std::min(T_2[2], t_far);
                if ((t_near > t_far) || (t_far < 0)) {
                    return false;
                }
            }
            return true; // if we made it here, there was an intersection - YAY
        }};

    auto isOverTriangle {
            [&](const ::glm::vec3 &vec) noexcept -> bool {
                const ::glm::vec3 &perpendicularVector {::glm::cross(vec, AC_)};
                const float normalizedProjection {::glm::dot(AB_, perpendicularVector)};
                const bool res{::std::abs(normalizedProjection) < Epsilon};
                return res;
            }
    };

    const ::glm::vec3 &min {box.pointMin_};
    const ::glm::vec3 &max {box.pointMax_};
    const ::glm::vec3 &vec {max - min};
    const Ray ray {vec, min, 1};
    const bool intersectedAB {intersectRayAABB(this->pointA_, this->AB_)};
    const bool intersectedAC {intersectRayAABB(this->pointA_, this->AC_)};
    const ::glm::vec3 &pointB {pointA_ + AB_};
    const ::glm::vec3 &pointC {pointA_ + AC_};
    const bool intersectedBC {intersectRayAABB(pointB, pointC - pointB)};
    Intersection intersection{RayLengthMax, nullptr};
    const float lastDist {intersection.length_};
    intersection = intersect(intersection, ray);
    const bool intersectedRay {intersection.length_ < lastDist};
    const bool insideTriangle {isOverTriangle(vec)};
    const bool res {intersectedAB || intersectedAC || intersectedBC || intersectedRay || insideTriangle};

    return res;
}
