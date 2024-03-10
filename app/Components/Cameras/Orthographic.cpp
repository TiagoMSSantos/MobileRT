#include "Components/Cameras/Orthographic.hpp"

using ::Components::Orthographic;
using ::MobileRT::AABB;
using ::MobileRT::Ray;

Orthographic::Orthographic(
    const ::glm::vec3 &position, const ::glm::vec3 &lookAt, const ::glm::vec3 &up,
    const float sizeH, const float sizeV) :
        Camera {position, lookAt, up},
        sizeH_ {sizeH / 2.0F},
        sizeV_ {sizeV / 2.0F} {
}

Ray Orthographic::generateRay(const float u, const float v,
                              const float deviationU, const float deviationV) const {
    const float rightFactor {(u - 0.5F) * this->sizeH_};
    const ::glm::vec3 &right {this->right_ * rightFactor + this->right_ * deviationU};
    const float upFactor {(0.5F - v) * this->sizeV_};
    const ::glm::vec3 &up {this->up_ * upFactor + this->up_ * deviationV};
    const Ray ray {this->direction_, this->position_ + right + up, 1, false};
    return ray;
}

AABB Orthographic::getAABB() const {
    const ::glm::vec3 &min {
        this->position_ +
        this->right_ * (0.0F - 0.5F) * this->sizeH_ + this->right_ * -0.5F +
        this->up_ * (0.5F - 0.0F) * this->sizeV_ + this->up_ * -0.5F
    };
    const ::glm::vec3 &max {
        this->position_ +
        this->right_ * (1.0F - 0.5F) * this->sizeH_ + this->right_ * 0.5F +
        this->up_ * (0.5F - 1.0F) * this->sizeV_ + this->up_ * 0.5F
    };
    const AABB res {min, max};
    return res;
}

float Orthographic::getSizeH() const {
    return this->sizeH_;
}

float Orthographic::getSizeV() const {
    return this->sizeV_;
}
