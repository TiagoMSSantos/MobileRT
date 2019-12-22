#include "MobileRT/Shapes/Sphere.hpp"
#include <algorithm>

using ::MobileRT::AABB;
using ::MobileRT::Sphere;
using ::MobileRT::Intersection;

Sphere::Sphere(const ::glm::vec3 &center, const float radius) noexcept :
        center_{center},
        sq_radius_{radius * radius} {
}

Intersection Sphere::intersect(const Intersection &intersection, const Ray &ray) const noexcept {
    //stackoverflow.com/questions/1986378/how-to-set-up-quadratic-equation-for-a-ray-sphere-intersection
    const ::glm::vec3 &originToCenter{center_ - ray.origin_};
    const float projectionOnDirection{::glm::dot(originToCenter, ray.direction_)};

    const float originToCenterMagnitude{::glm::length(originToCenter)};
    //A = 1.0 - normalized vectors
    const float A{::glm::dot(ray.direction_, ray.direction_)};
    const float B{2.0f * -projectionOnDirection};
    const float C{originToCenterMagnitude * originToCenterMagnitude - this->sq_radius_};
    const float discriminant{B * B - 4.0f * A * C};
    //don't intersect (ignores tangent point of the sphere)
    if (discriminant < 0.0f) {
        return intersection;
    }

    //if discriminant > 0 - ray intersects the sphere in 2 points
    //if discriminant == 0 - ray intersects the sphere in 1 point
    const float rootDiscriminant {::std::sqrt(discriminant)};
    const float distanceToIntersection1 {-B + rootDiscriminant};
    const float distanceToIntersection2 {-B - rootDiscriminant};
    //distance between intersection and camera = smaller root = closer intersection
    const float distanceToIntersection{
            ::std::min(distanceToIntersection1, distanceToIntersection2) / (2.0f * A)};

    if (distanceToIntersection < 1.0e-05f || distanceToIntersection >= intersection.length_) {
        return intersection;
    }

    // if so, then we have an intersection
    const ::glm::vec3 &intersectionPoint {ray.origin_ + ray.direction_ * distanceToIntersection};
    const ::glm::vec3 &intersectionNormal{::glm::normalize(intersectionPoint - center_)};
    const Intersection res {intersectionPoint, distanceToIntersection, intersectionNormal, nullptr};
    return res;
}

AABB Sphere::getAABB() const noexcept {
    const float radius {::std::sqrt(this->sq_radius_)};
    const ::glm::vec3 &min {this->center_ - radius};
    const ::glm::vec3 &max {this->center_ + radius};
    const AABB res {min, max};
    return res;
}

bool Sphere::intersect(const AABB &box) const noexcept {
    float dmin {};
    const ::glm::vec3 &v1 {box.pointMin_};
    const ::glm::vec3 &v2 {box.pointMax_};
    if (center_[0] < v1[0]) {
        dmin = dmin + (center_[0] - v1[0]) * (center_[0] - v1[0]);
    } else if (center_[0] > v2[0]) {
        dmin = dmin + (center_[0] - v2[0]) * (center_[0] - v2[0]);
    }
    if (center_[1] < v1[1]) {
        dmin = dmin + (center_[1] - v1[1]) * (center_[1] - v1[1]);
    } else if (center_[1] > v2[1]) {
        dmin = dmin + (center_[1] - v2[1]) * (center_[1] - v2[1]);
    }
    if (center_[2] < v1[2]) {
        dmin = dmin + (center_[2] - v1[2]) * (center_[2] - v1[2]);
    } else if (center_[2] > v2[2]) {
        dmin = dmin + (center_[2] - v2[2]) * (center_[2] - v2[2]);
    }
    const bool res {(dmin <= sq_radius_)};
    return res;
}
