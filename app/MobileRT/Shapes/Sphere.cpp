#include "MobileRT/Shapes/Sphere.hpp"
#include <algorithm>

using ::MobileRT::AABB;
using ::MobileRT::Sphere;
using ::MobileRT::Intersection;

Sphere::Sphere(const ::glm::vec3 &center, const float radius) noexcept :
    center_ {center},
    sqRadius_ {radius * radius} {
}

Intersection Sphere::intersect(const Intersection &intersection, const Ray &ray) const noexcept {
    //stackoverflow.com/questions/1986378/how-to-set-up-quadratic-equation-for-a-ray-sphere-intersection
    const auto &originToCenter {this->center_ - ray.origin_};
    const auto projectionOnDirection {::glm::dot(originToCenter, ray.direction_)};

    const auto originToCenterMagnitude {::glm::length(originToCenter)};
    //a = 1.0 - normalized vectors
    const auto a {::glm::dot(ray.direction_, ray.direction_)};
    const auto b {2.0f * -projectionOnDirection};
    const auto c {originToCenterMagnitude * originToCenterMagnitude - this->sqRadius_};
    const auto discriminant {b * b - 4.0f * a * c};
    //don't intersect (ignores tangent point of the sphere)
    if (discriminant < 0.0f) {
        return intersection;
    }

    //if discriminant > 0 - ray intersects the sphere in 2 points
    //if discriminant == 0 - ray intersects the sphere in 1 point
    const auto rootDiscriminant {::std::sqrt(discriminant)};
    const auto distanceToIntersection1 {-b + rootDiscriminant};
    const auto distanceToIntersection2 {-b - rootDiscriminant};
    //distance between intersection and camera = smaller root = closer intersection
    const auto distanceToIntersection {::std::min(distanceToIntersection1, distanceToIntersection2) / (2.0f * a)};

    if (distanceToIntersection < 1.0e-05f || distanceToIntersection >= intersection.length_) {
        return intersection;
    }

    // if so, then we have an intersection
    const auto &intersectionPoint {ray.origin_ + ray.direction_ * distanceToIntersection};
    const auto &intersectionNormal {::glm::normalize(intersectionPoint - this->center_)};
    const Intersection res {intersectionPoint, distanceToIntersection, intersectionNormal, nullptr};
    return res;
}

AABB Sphere::getAABB() const noexcept {
    const auto radius {::std::sqrt(this->sqRadius_)};
    const auto &min {this->center_ - radius};
    const auto &max {this->center_ + radius};
    const AABB &res {min, max};
    return res;
}

bool Sphere::intersect(const AABB &box) const noexcept {
    auto dmin {0.0F};
    const auto &v1 {box.pointMin_};
    const auto &v2 {box.pointMax_};
    if (this->center_[0] < v1[0]) {
        dmin = dmin + (this->center_[0] - v1[0]) * (this->center_[0] - v1[0]);
    } else if (this->center_[0] > v2[0]) {
        dmin = dmin + (this->center_[0] - v2[0]) * (this->center_[0] - v2[0]);
    }
    if (this->center_[1] < v1[1]) {
        dmin = dmin + (this->center_[1] - v1[1]) * (this->center_[1] - v1[1]);
    } else if (this->center_[1] > v2[1]) {
        dmin = dmin + (this->center_[1] - v2[1]) * (this->center_[1] - v2[1]);
    }
    if (this->center_[2] < v1[2]) {
        dmin = dmin + (this->center_[2] - v1[2]) * (this->center_[2] - v1[2]);
    } else if (this->center_[2] > v2[2]) {
        dmin = dmin + (this->center_[2] - v2[2]) * (this->center_[2] - v2[2]);
    }
    const auto res {(dmin <= this->sqRadius_)};
    return res;
}
