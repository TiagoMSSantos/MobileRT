#include "Components/Lights/PointLight.hpp"

using ::Components::PointLight;
using ::MobileRT::Material;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;

PointLight::PointLight(const Material &radiance, const ::glm::vec3 &position) noexcept :
        Light {radiance},
        position_ {position} {
}

::glm::vec3 PointLight::getPosition() noexcept {
    return this->position_;
}

void PointLight::resetSampling() noexcept {
}

Intersection PointLight::intersect(Intersection intersection, const Ray &/*ray*/) const noexcept {
    return intersection;
}
