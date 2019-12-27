#include "Components/Shaders/NoShadows.hpp"
#include <glm/glm.hpp>

using ::Components::NoShadows;
using ::MobileRT::Light;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Scene;

NoShadows::NoShadows(Scene scene, const ::std::int32_t samplesLight, const Accelerator accelerator) noexcept :
    Shader {::std::move(scene), samplesLight, accelerator} {
}

bool NoShadows::shade(::glm::vec3 *const rgb, const Intersection &intersection, const Ray &/*ray*/) noexcept {
    const auto &lE {intersection.material_->Le_};
    //stop if it intersects a light source
    if (::glm::any(::glm::greaterThan(lE, ::glm::vec3 {0}))) {
        *rgb = lE;
        return true;
    }

    const auto &kD {intersection.material_->Kd_};
    const auto &shadingNormal {intersection.normal_};

    // direct lighting - only for diffuse materials
    if (::glm::any(::glm::greaterThan(kD, ::glm::vec3 {0}))) {
        const auto sizeLights {this->lights_.size()};
        if (sizeLights > 0) {
            const auto samplesLight {this->samplesLight_};
            for (::std::int32_t j {}; j < samplesLight; ++j) {
                const auto chosenLight {getLightIndex()};
                auto &light {*this->lights_[chosenLight]};
                const auto &lightPosition {light.getPosition()};
                //vectorIntersectCameraNormalized = light.position_ - intersection.point_
                const auto &vectorToLightNormalized {::glm::normalize(lightPosition - intersection.point_)};
                const auto cosNl {::glm::dot(shadingNormal, vectorToLightNormalized)};
                if (cosNl > 0.0f) {
                    //rgb += kD * radLight * cosNl;
                    *rgb += light.radiance_.Le_ * cosNl;
                }
            }
            *rgb *= kD;
            *rgb /= samplesLight;
        } // end direct
    }
    *rgb += kD * 0.1f;//ambient light
    return false;
}
