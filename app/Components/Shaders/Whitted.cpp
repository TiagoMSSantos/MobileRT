#include "Components/Shaders/Whitted.hpp"

using ::Components::Whitted;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Scene;
using ::MobileRT::RayDepthMax;

Whitted::Whitted(Scene scene, const ::std::int32_t samplesLight, Accelerator accelerator) :
    Shader {::std::move(scene), samplesLight, accelerator} {
}

bool Whitted::shade(::glm::vec3 *const rgb, const Intersection &intersection) {
    const ::std::int32_t rayDepth {intersection.ray_.depth_};
    if (rayDepth > RayDepthMax) {
        return false;
    }

    const ::glm::vec3 &lE {intersection.material_->Le_};
    //STOP if it intersects a light source
    if (::MobileRT::hasPositiveValue(lE)) {
        *rgb = lE;
        return true;
    }

    const ::glm::vec3 &kD {intersection.material_->Kd_};
    const ::glm::vec3 &kS {intersection.material_->Ks_};
    const ::glm::vec3 &kT {intersection.material_->Kt_};

    // the normal always points to outside objects (e.g., spheres)
    // if the cosine between the ray and the normal is less than 0 then
    // the ray intersected the object from the inside and the shading normal
    // should be symmetric to the geometric normal
    const ::glm::vec3 &shadingNormal {intersection.normal_};

    // shadowed direct lighting - only for diffuse materials
    if (::MobileRT::hasPositiveValue(kD)) {
        const long long unsigned sizeLights {this->lights_.size()};
        if (sizeLights > 0) {
            const ::std::int32_t samplesLight {this->samplesLight_};
            for (::std::int32_t i {}; i < samplesLight; ++i) {
                const ::std::uint32_t chosenLight {getLightIndex()};
                ::MobileRT::Light &light {*this->lights_[chosenLight]};
                const ::glm::vec3 lightPosition {light.getPosition()};
                //calculates vector starting in intersection to the light
                ::glm::vec3 vectorToLight {lightPosition - intersection.point_};
                //distance from intersection to the light (and normalize it)
                const float distanceToLight {::glm::length(vectorToLight)};
                vectorToLight = ::glm::normalize(vectorToLight);
                const float cosNl {::glm::dot(shadingNormal, vectorToLight)};
                if (cosNl > 0.0F) {
                    //shadow ray - orig=intersection, dir=light
                    Ray shadowRay {vectorToLight, intersection.point_, rayDepth + 1, true, intersection.primitive_};
                    //intersection between shadow ray and the closest primitive
                    //if there are no primitives between intersection and the light
                    if (!shadowTrace(distanceToLight, ::std::move(shadowRay))) {
                        // "rgb += kD * radLight * cosNl;"
                        *rgb += light.radiance_.Le_ * cosNl;
                    }
                }
            }
            *rgb *= kD;
            *rgb /= samplesLight;
        } // end direct + ambient
    }

    const float destIor {intersection.material_->refractiveIndice_};
    const float sourceIor {1.0F};
    const float ior {sourceIor / destIor};
    const float kr {::MobileRT::fresnel(intersection.ray_.direction_, shadingNormal, destIor)};

    // specular reflection
    if (::MobileRT::hasPositiveValue(kS)) {
        const ::glm::vec3 &reflectionDir {::glm::reflect(intersection.ray_.direction_, shadingNormal)};
        Ray specularRay {reflectionDir, intersection.point_, rayDepth + 1, false, intersection.primitive_};
        ::glm::vec3 LiS_RGB {};
        rayTrace(&LiS_RGB, ::std::move(specularRay));
        *rgb += kS * LiS_RGB;
    }

    // specular transmission
    if (::MobileRT::hasPositiveValue(kT)) {
        const float kt {1.0F - kr};
        const ::glm::vec3 &refractDir {::glm::refract(intersection.ray_.direction_, shadingNormal, ior)};
        Ray transmissionRay {refractDir, intersection.point_, rayDepth + 1, false, intersection.primitive_};
        ::glm::vec3 LiT_RGB {};
        rayTrace(&LiT_RGB, ::std::move(transmissionRay));
        static_cast<void>(kt);
        *rgb += kT * LiT_RGB;
    }
    *rgb += kD *  0.1F;//ambient light
    return false;
}
