#include "Components/Shaders/NoShadows.hpp"
#include <glm/glm.hpp>

using ::Components::NoShadows;
using ::MobileRT::Light;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Scene;

NoShadows::NoShadows(Scene scene, const ::std::uint32_t samplesLight,
                     const Accelerator accelerator) noexcept :
        Shader{::std::move(scene), samplesLight, accelerator} {
}

bool NoShadows::shade(
        ::glm::vec3 *const rgb, const Intersection &intersection, const Ray &/*ray*/) noexcept {

    const ::glm::vec3 &Le {intersection.material_->Le_};
    //stop if it intersects a light source
    if (::glm::any(::glm::greaterThan(Le, ::glm::vec3 {0}))) {
        *rgb = Le;
        return true;
    }

    const ::glm::vec3 &kD {intersection.material_->Kd_};

    const ::glm::vec3 &shadingNormal{intersection.normal_};

    // direct lighting - only for diffuse materials
    if (::glm::any(::glm::greaterThan(kD, ::glm::vec3 {0}))) {
        const ::std::uint32_t sizeLights {
            static_cast<::std::uint32_t>(scene_.lights_.size())};
        if (sizeLights > 0) {
            const ::std::uint32_t samplesLight {this->samplesLight_};
            for (::std::uint32_t j {}; j < samplesLight; ++j) {
                const ::std::uint32_t chosenLight {getLightIndex()};
                Light &light(*scene_.lights_[chosenLight]);
                const ::glm::vec3 &lightPosition {light.getPosition()};
                //vectorIntersectCameraNormalized = light.position_ - intersection.point_
                const ::glm::vec3 &vectorToLightNormalized {
                        ::glm::normalize(lightPosition - intersection.point_)};
                const float cos_N_L {::glm::dot(shadingNormal, vectorToLightNormalized)};
                if (cos_N_L > 0.0f) {
                    //rgb += kD * radLight * cos_N_L;
                    *rgb += light.radiance_.Le_ * cos_N_L;
                }
            }
            *rgb *= kD;
            *rgb /= samplesLight;
        } // end direct
    }
    *rgb += kD * 0.1f;//ambient light
    return false;
}
