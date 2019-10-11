//
// Created by Tiago on 22-Jun-17.
//

#include "Components/Cameras/Orthographic.hpp"

using ::Components::Orthographic;
using ::MobileRT::AABB;
using ::MobileRT::Ray;

Orthographic::Orthographic(
    const ::glm::vec3 &position, const ::glm::vec3 &lookAt, const ::glm::vec3 &up,
    const float sizeH, const float sizeV) noexcept :
        Camera(position, lookAt, up),
        sizeH_{sizeH / 2.0f},
        sizeV_{sizeV / 2.0f} {
}

/* u = x / width */
/* v = y / height */
/* deviationU = [-0.5f / width, 0.5f / width] */
/* deviationV = [-0.5f / height, 0.5f / height] */
Ray Orthographic::generateRay(const float u, const float v,
                              const float deviationU, const float deviationV) const noexcept {
    const float rightFactor{(u - 0.5f) * this->sizeH_};
    const ::glm::vec3 &right{this->right_ * rightFactor + this->right_ * deviationU};
    const float upFactor{(0.5f - v) * this->sizeV_};
    const ::glm::vec3 &up{this->up_ * upFactor + this->up_ * deviationV};
    const Ray ray {this->direction_, this->position_ + right + up, 1};
    return ray;
}

AABB Orthographic::getAABB() const noexcept {
    const ::glm::vec3 &min {this->position_ +
                      this->right_ * (0.0f - 0.5f) * this->sizeH_ + this->right_ * -0.5f +
                      this->up_ * (0.5f - 0.0f) * this->sizeV_ + this->up_ * -0.5f};
    const ::glm::vec3 &max {this->position_ +
                      this->right_ * (1.0f - 0.5f) * this->sizeH_ + this->right_ * 0.5f +
                      this->up_ * (0.5f - 1.0f) * this->sizeV_ + this->up_ * 0.5f};
    const AABB res {min, max};
    return res;
}

float Orthographic::getSizeH() const noexcept {
    return sizeH_;
}

float Orthographic::getSizeV() const noexcept {
    return sizeV_;
}
