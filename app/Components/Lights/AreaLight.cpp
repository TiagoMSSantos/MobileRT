#include "Components/Lights/AreaLight.hpp"

using ::Components::AreaLight;
using ::MobileRT::Material;
using ::MobileRT::Sampler;
using ::MobileRT::Intersection;

AreaLight::AreaLight(
    const Material &radiance,
    ::std::unique_ptr<Sampler> samplerPointLight,
    const ::MobileRT::Triangle &triangle) :
        Light {radiance},
        triangle_ {triangle},
        samplerPointLight_ {::std::move(samplerPointLight)} {
}

::glm::vec3 AreaLight::getPosition() {
    auto r {this->samplerPointLight_->getSample()};
    auto s {this->samplerPointLight_->getSample()};
    if (r + s >= 1.0F) {
        r = 1.0F - r;
        s = 1.0F - s;
    }
    const auto &position {this->triangle_.getA() + r * this->triangle_.getAB() + s * this->triangle_.getAC()};
    return position;
}

void AreaLight::resetSampling() {
    this->samplerPointLight_->resetSampling();
}

Intersection AreaLight::intersect(Intersection &&intersection) {
    const auto lastDist {intersection.length_};
    intersection = this->triangle_.intersect(intersection);
    const auto intersected {intersection.length_ < lastDist};
    if (intersected) {
        intersection.material_ = &this->radiance_;
        intersection.materialIndex_ = -1;
    }
    return ::std::move(intersection);
}
