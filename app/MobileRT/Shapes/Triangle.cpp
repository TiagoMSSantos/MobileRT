#include "MobileRT/Shapes/Triangle.hpp"

using ::MobileRT::AABB;
using ::MobileRT::Triangle;
using ::MobileRT::Intersection;

Triangle::Triangle(const ::glm::vec3 &pointA, const ::glm::vec3 &pointB, const ::glm::vec3 &pointC) noexcept :
    AC_ {pointC - pointA},
    AB_ {pointB - pointA},
    pointA_ {pointA} {
}

Intersection Triangle::intersect(const Intersection &intersection, const Ray &ray) const noexcept {
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
    const auto &intersectionNormal1 {::glm::normalize(::glm::cross(this->AB_, this->AC_))};
    const auto &intersectionNormal2 {::glm::normalize(::glm::cross(this->AC_, this->AB_))};
    const auto &intersectionNormal {::glm::dot(intersectionNormal1, ray.direction_) < 0.0F
        ? intersectionNormal1
        : intersectionNormal2
    };

    const auto& intersectionPoint {ray.origin_ + ray.direction_ * distanceToIntersection};
    const Intersection res {intersectionPoint, distanceToIntersection, intersectionNormal, this};
    return res;
}

AABB Triangle::getAABB() const noexcept {
    const auto &pointB {this->pointA_ + this->AB_};
    const auto &pointC {this->pointA_ + this->AC_};
    const auto &min {::glm::min(this->pointA_, ::glm::min(pointB, pointC))};
    const auto &max {::glm::max(this->pointA_, ::glm::max(pointB, pointC))};
    const AABB &res {min, max};
    return res;
}

bool Triangle::intersect(const AABB &box) const noexcept {
    auto intersectRayAABB {
        [&](const ::glm::vec3 &orig, const ::glm::vec3 &vec) noexcept -> bool {
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
        [&](const ::glm::vec3 &vec) noexcept -> bool {
            const ::glm::vec3 &perpendicularVector {::glm::cross(vec, this->AC_)};
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
    Intersection intersection {RayLengthMax, nullptr};
    const auto lastDist {intersection.length_};
    intersection = intersect(intersection, ray);
    const auto intersectedRay {intersection.length_ < lastDist};
    const auto insideTriangle {isOverTriangle(vec)};
    const auto res {intersectedAB || intersectedAC || intersectedBC || intersectedRay || insideTriangle};

    return res;
}
