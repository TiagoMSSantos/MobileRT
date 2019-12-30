#include "MobileRT/Camera.hpp"
#include <glm/gtc/constants.hpp>

using ::MobileRT::AABB;
using ::MobileRT::Camera;

//Left hand rule
Camera::Camera(const ::glm::vec3 &position, const ::glm::vec3 &lookAt, const ::glm::vec3 &up) :
        position_ {position},
        direction_ {::glm::normalize(lookAt - position)},
        right_ {::glm::cross(up, direction_)},
        up_ {::glm::cross(direction_, right_)} {
}

Camera::~Camera() {
    LOG("CAMERA DESTROYED!!!");
}

Camera::Camera(const Camera &camera) {
    this->position_ = camera.position_;
    this->direction_ = camera.direction_;
    this->right_ = camera.right_;
    this->up_ = camera.up_;
}

float Camera::degToRad(const float deg) const {
    const auto radians {(deg * ::glm::pi<float>()) / 180.0f};
    return radians;
}

float Camera::radToDeg(const float rad) const {
    const auto degrees {(rad / ::glm::pi<float>()) * 180.0f};
    return degrees;
}

AABB Camera::getAABB() const {
    const auto &min {this->position_};
    const auto &max {this->position_};
    const AABB &res {min, max};
    return res;
}
