#include "Components/Lights/PointLight.hpp"

using ::Components::PointLight;
using ::MobileRT::Material;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;

PointLight::PointLight(const Material &radiance, const ::glm::vec3 &position) :
        Light {radiance},
        position_ {position} {
}

::glm::vec3 PointLight::getPosition() {
    return this->position_;
}

void PointLight::resetSampling() {
}

Intersection PointLight::intersect(Intersection intersection) {
    return intersection;
}
