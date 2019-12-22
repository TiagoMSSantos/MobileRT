#include "MobileRT/Camera.hpp"
#include <glm/gtc/constants.hpp>

using ::MobileRT::AABB;
using ::MobileRT::Camera;

AABB Camera::getAABB() const noexcept {
    const ::glm::vec3 &min {position_};
    const ::glm::vec3 &max {position_};
    const AABB res {min, max};
    return res;
}

//Left hand rule
Camera::Camera(const ::glm::vec3 &position, const ::glm::vec3 &lookAt, const ::glm::vec3 &up) noexcept :
        position_{position},
        direction_{::glm::normalize(lookAt - position)},
        right_{::glm::cross(up, direction_)},
        up_{::glm::cross(direction_,right_)} {
}

Camera::Camera(const Camera &camera) noexcept {
    this->position_ = camera.position_;
    this->direction_ = camera.direction_;
    this->right_ = camera.right_;
    this->up_ = camera.up_;
}

Camera::~Camera() noexcept {
    LOG("CAMERA DELETED");
}

float Camera::degToRad(const float deg) const noexcept {
    const float radians{(deg * ::glm::pi<float>()) / 180.0f};
    return radians;
}

float Camera::radToDeg(const float rad) const noexcept {
    const float degrees{(rad / ::glm::pi<float>()) * 180.0f};
    return degrees;
}
