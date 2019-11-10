#include "Components/Cameras/Perspective.hpp"
#include <glm/glm.hpp>
#include <glm/gtc/constants.hpp>

using ::Components::Perspective;
using ::MobileRT::Ray;

Perspective::Perspective(
    const ::glm::vec3 &position, const ::glm::vec3 &lookAt, const ::glm::vec3 &up,
    const float hFov, const float vFov) noexcept :
        Camera(position, lookAt, up),
        hFov_{degToRad(hFov)},
        vFov_{degToRad(vFov)} {
}

/* u = x / width */
/* v = y / height */
/* deviationU = [-0.5F / width, 0.5F / width] */
/* deviationV = [-0.5F / height, 0.5F / height] */
Ray Perspective::generateRay(const float u, const float v,
                             const float deviationU, const float deviationV) const noexcept {
    const float tanValueRight{this->hFov_ * (u - 0.5F)};
    const float rightFactor{fastArcTan(tanValueRight) + deviationU};
    const ::glm::vec3 &right{this->right_ * rightFactor};
    const float tanValueUp{this->vFov_ * (0.5F - v)};
    const float upFactor{fastArcTan(tanValueUp) + deviationV};
    const ::glm::vec3 &up{this->up_ * upFactor};
    const ::glm::vec3 &dest{this->position_ + this->direction_ + right + up};
    const ::glm::vec3 &rayDirection{::glm::normalize(dest - position_)};
    const Ray ray {rayDirection, this->position_, 1};
    return ray;
}

//http://nghiaho.com/?p=997
float Perspective::fastArcTan(const float value) const noexcept {
    const float absValue{::std::abs(value)};
    const float res{::glm::quarter_pi<float>() * value -
                    (value * (absValue - 1.0F)) * (0.2447F + (0.0663F * absValue))};
    return res;
}

float Perspective::getHFov() const noexcept {
    const float degrees{radToDeg(hFov_)};
    return degrees;
}

float Perspective::getVFov() const noexcept {
    const float degrees{radToDeg(vFov_)};
    return degrees;
}
