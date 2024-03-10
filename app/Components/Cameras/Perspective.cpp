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

Ray Perspective::generateRay(const float u, const float v,
                             const float deviationU, const float deviationV) const {
    const float tanValueRight {this->hFov_ * (u - 0.5F)};
    const float rightFactor {fastArcTan(tanValueRight) + deviationU};
    const ::glm::vec3 &right {this->right_ * rightFactor};
    const float tanValueUp {this->vFov_ * (0.5F - v)};
    const float upFactor {fastArcTan(tanValueUp) + deviationV};
    const ::glm::vec3 &up {this->up_ * upFactor};
    const ::glm::vec3 &dest {this->position_ + this->direction_ + right + up};
    const ::glm::vec3 &rayDirection {::glm::normalize(dest - position_)};
    const Ray ray {rayDirection, this->position_, 1, false};
    return ray;
}

/**
 * Helper method that calculates the inverse tangent function.
 * This is an approximate algorithm from
 * <a href="http://nghiaho.com/?p=997">
 * this source
 * </a>.
 *
 * @param value The value to calculate the arc tangent.
 * @return The principal arc tangent of a value, in the interval [-pi/2,+pi/2] radians.
 */
float Perspective::fastArcTan(const float value) {
    const float absValue {::std::abs(value)};
    const float res {
        ::glm::quarter_pi<float>() * value - (value * (absValue - 1.0F)) * (0.2447F + (0.0663F * absValue))
    };
    return res;
}

/**
 * Gets the horizontal field of view, in degrees.
 *
 * @return The horizontal field of view, in degrees.
 */
float Perspective::getHFov() const {
    const float degrees {radToDeg(this->hFov_)};
    return degrees;
}

/**
 * Gets the vertical field of view, in degrees.
 *
 * @return The vertical field of view, in degrees.
 */
float Perspective::getVFov() const {
    const float degrees {radToDeg(this->vFov_)};
    return degrees;
}
