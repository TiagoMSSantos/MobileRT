#include "Components/Shaders/NoShadows.hpp"
#include <glm/glm.hpp>

using ::Components::NoShadows;
using ::MobileRT::Intersection;
using ::MobileRT::Scene;

NoShadows::NoShadows(Scene scene, const ::std::int32_t samplesLight, const Accelerator accelerator) :
    Shader {::std::move(scene), samplesLight, accelerator} {
}

bool NoShadows::shade(::glm::vec3 *const rgb, const Intersection &intersection) {
    const ::glm::vec3 &lE {intersection.material_->Le_};
    //stop if it intersects a light source
    if (::MobileRT::hasPositiveValue(lE)) {
        *rgb = lE;
        return true;
    }

    const ::glm::vec3 &kD {intersection.material_->Kd_};
    const ::glm::vec3 &shadingNormal {intersection.normal_};

    // direct lighting - only for diffuse materials
    if (::MobileRT::hasPositiveValue(kD)) {
        const long long unsigned sizeLights {this->lights_.size()};
        if (sizeLights > 0) {
            const ::std::int32_t samplesLight {this->samplesLight_};
            for (::std::int32_t j {}; j < samplesLight; ++j) {
                const ::std::uint32_t chosenLight {getLightIndex()};
                ::MobileRT::Light &light {*this->lights_[chosenLight]};
                const ::glm::vec3 &lightPosition {light.getPosition()};
                //vectorIntersectCameraNormalized = light.position_ - intersection.point_
                const ::glm::vec3 &vectorToLightNormalized {::glm::normalize(lightPosition - intersection.point_)};
                const float cosNl {::glm::dot(shadingNormal, vectorToLightNormalized)};
                if (cosNl > 0.0F) {
                    // "rgb += kD * radLight * cosNl;"
                    *rgb += light.radiance_.Le_ * cosNl;
                }
            }
            *rgb *= kD;
            *rgb /= samplesLight;
        } // end direct
    }
    *rgb += kD * 0.1F;//ambient light
    return false;
}
