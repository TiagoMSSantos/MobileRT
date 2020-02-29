#include "MobileRT/Camera.hpp"
#include <glm/gtc/constants.hpp>

using ::MobileRT::AABB;
using ::MobileRT::Camera;

/**
 * The constructor.
 *
 * @param position The position of the camera.
 * @param lookAt   The point where the camera is looking at.
 * @param up       The up vector.
 */
Camera::Camera(const ::glm::vec3 &position, const ::glm::vec3 &lookAt, const ::glm::vec3 &up) :
        position_ {position},
        direction_ {::glm::normalize(lookAt - position)},
        right_ {::glm::cross(up, direction_)},
        up_ {::glm::cross(direction_, right_)} {
}

/**
 * The destructor.
 */
Camera::~Camera() {
    LOG("CAMERA DESTROYED!!!");
}

/**
 * The copy constructor.
 *
 * @param camera A camera to copy.
 */
Camera::Camera(const Camera &camera) {
    this->position_ = camera.position_;
    this->direction_ = camera.direction_;
    this->right_ = camera.right_;
    this->up_ = camera.up_;
}

/**
 * A helper method which converts degrees to radians.
 *
 * @param deg The number of degrees.
 * @return The number of radians.
 */
float Camera::degToRad(const float deg) const {
    const auto radians {(deg * ::glm::pi<float>()) / 180.0f};
    return radians;
}

/**
 * A helper method which converts radians to degrees.
 *
 * @param rad The number of radians.
 * @return The number of degrees.
 */
float Camera::radToDeg(const float rad) const {
    const auto degrees {(rad / ::glm::pi<float>()) * 180.0f};
    return degrees;
}

/**
 * Calculates the bounding box of the camera.
 *
 * @return The bounding box which surrounds the camera.
 */
AABB Camera::getAABB() const {
    const auto &min {this->position_};
    const auto &max {this->position_};
    const AABB &res {min, max};
    return res;
}
