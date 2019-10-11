//
// Created by puscas on 03-03-2017.
//

#include "Components/Lights/AreaLight.hpp"

using ::Components::AreaLight;
using ::MobileRT::Material;
using ::MobileRT::Sampler;
using ::MobileRT::Ray;
using ::MobileRT::Intersection;

AreaLight::AreaLight(
    const Material &radiance,
    ::std::unique_ptr<Sampler> samplerPointLight,
    const ::glm::vec3 &pointA, const ::glm::vec3 &pointB, const ::glm::vec3 &pointC) noexcept :
        Light(radiance),
        triangle_{pointA, pointB, pointC},
        samplerPointLight_{::std::move(samplerPointLight)} {
}

::glm::vec3 AreaLight::getPosition() noexcept {
    float R{samplerPointLight_->getSample()};
    float S{samplerPointLight_->getSample()};
    if (R + S >= 1.0f) {
        R = 1.0f - R;
        S = 1.0f - S;
    }
    const ::glm::vec3 &position{triangle_.pointA_ + R * triangle_.AB_ + S * triangle_.AC_};
    return position;
}

void AreaLight::resetSampling() noexcept {
    samplerPointLight_->resetSampling();
}

Intersection AreaLight::intersect(Intersection intersection, const Ray &ray) const noexcept {
    const float lastDist {intersection.length_};
    intersection = triangle_.intersect(intersection, ray);
    intersection.material_ = intersection.length_ < lastDist? &radiance_ : intersection.material_;
    return intersection;
}
