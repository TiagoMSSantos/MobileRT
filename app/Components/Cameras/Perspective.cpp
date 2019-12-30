#include "Components/Cameras/Perspective.hpp"
#include <glm/glm.hpp>
#include <glm/gtc/constants.hpp>

using ::Components::Perspective;
using ::MobileRT::Ray;

Perspective::Perspective(
    const ::glm::vec3 &position, const ::glm::vec3 &lookAt, const ::glm::vec3 &up,
    const float hFov, const float vFov) :
        Camera {position, lookAt, up},
        hFov_ {degToRad(hFov)},
        vFov_ {degToRad(vFov)} {
}

/* u = x / width */
/* v = y / height */
/* deviationU = [-0.5F / width, 0.5F / width] */
/* deviationV = [-0.5F / height, 0.5F / height] */
Ray Perspective::generateRay(const float u, const float v,
                             const float deviationU, const float deviationV) const {
    const auto tanValueRight {this->hFov_ * (u - 0.5F)};
    const auto rightFactor {fastArcTan(tanValueRight) + deviationU};
    const auto &right {this->right_ * rightFactor};
    const auto tanValueUp {this->vFov_ * (0.5F - v)};
    const auto upFactor {fastArcTan(tanValueUp) + deviationV};
    const auto &up {this->up_ * upFactor};
    const auto &dest {this->position_ + this->direction_ + right + up};
    const auto &rayDirection {::glm::normalize(dest - position_)};
    const Ray &ray {rayDirection, this->position_, 1};
    return ray;
}

//http://nghiaho.com/?p=997
float Perspective::fastArcTan(const float value) const {
    const auto absValue {::std::abs(value)};
    const auto res {
        ::glm::quarter_pi<float>() * value - (value * (absValue - 1.0F)) * (0.2447F + (0.0663F * absValue))
    };
    return res;
}

float Perspective::getHFov() const {
    const auto degrees {radToDeg(this->hFov_)};
    return degrees;
}

float Perspective::getVFov() const {
    const auto degrees {radToDeg(this->vFov_)};
    return degrees;
}
