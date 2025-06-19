#include "Components/Lights/AreaLight.hpp"

using ::Components::AreaLight;
using ::MobileRT::Material;
using ::MobileRT::Sampler;
using ::MobileRT::Intersection;

AreaLight::AreaLight(
    Material radiance,
    ::std::unique_ptr<Sampler> samplerPointLight,
    ::MobileRT::Triangle &&triangle) :
        Light {::std::move(radiance)},
        triangle_ {::std::move(triangle)},
        samplerPointLight_ {::std::move(samplerPointLight)} {
}

::glm::vec3 AreaLight::getPosition() {
    float r {this->samplerPointLight_->getSample()};
    float s {this->samplerPointLight_->getSample()};
    if (r + s >= 1.0F) {
        r = 1.0F - r;
        s = 1.0F - s;
    }
    const ::glm::vec3 &position {this->triangle_.getA() + r * this->triangle_.getAB() + s * this->triangle_.getAC()};
    return position;
}

void AreaLight::resetSampling() {
    this->samplerPointLight_->resetSampling();
}

Intersection AreaLight::intersect(Intersection &&intersection) {
    const float lastDist {intersection.length_};
    intersection = this->triangle_.intersect(intersection);
    const bool intersected {intersection.length_ < lastDist};
    if (intersected) {
        intersection.material_ = &this->radiance_;
        intersection.materialIndex_ = -1;
    }
    return ::std::move(intersection);
}
