#include "Components/Shaders/Whitted.hpp"

using ::Components::Whitted;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Scene;
using ::MobileRT::RayDepthMax;

Whitted::Whitted(Scene scene, const ::std::int32_t samplesLight, Accelerator accelerator) :
    Shader {::std::move(scene), samplesLight, accelerator} {
}

bool Whitted::shade(::glm::vec3 *const rgb, const Intersection &intersection, const Ray &ray) {
    const auto rayDepth {ray.depth_};
    if (rayDepth > RayDepthMax) {
        return false;
    }

    const auto &lE {intersection.material_->Le_};
    //STOP if it intersects a light source
    if (::glm::any(::glm::greaterThan(lE, ::glm::vec3 {0}))) {
        *rgb = lE;
        return true;
    }

    const auto &kD {intersection.material_->Kd_};
    const auto &kS {intersection.material_->Ks_};
    const auto &kT {intersection.material_->Kt_};

    // the normal always points to outside objects (e.g., spheres)
    // if the cosine between the ray and the normal is less than 0 then
    // the ray intersected the object from the inside and the shading normal
    // should be symmetric to the geometric normal
    const auto &shadingNormal {intersection.normal_};

    // shadowed direct lighting - only for diffuse materials
    if (::glm::any(::glm::greaterThan(kD, ::glm::vec3 {0}))) {
        const auto sizeLights {this->lights_.size()};
        if (sizeLights > 0) {
            const auto samplesLight {this->samplesLight_};
            for (::std::int32_t i {}; i < samplesLight; ++i) {
                const auto chosenLight {getLightIndex()};
                auto &light {*this->lights_[chosenLight]};
                const auto lightPosition {light.getPosition()};
                //calculates vector starting in intersection to the light
                auto vectorToLight {lightPosition - intersection.point_};
                //distance from intersection to the light (and normalize it)
                const auto distanceToLight {::glm::length(vectorToLight)};
                vectorToLight = ::glm::normalize(vectorToLight);
                const auto cosNl {::glm::dot(shadingNormal, vectorToLight)};
                if (cosNl > 0.0F) {
                    //shadow ray - orig=intersection, dir=light
                    const Ray &shadowRay {vectorToLight, intersection.point_, rayDepth + 1, intersection.primitive_};
                    Intersection lightIntersection {distanceToLight};
                    //intersection between shadow ray and the closest primitive
                    //if there are no primitives between intersection and the light
                    if (!shadowTrace(lightIntersection, shadowRay)) {
                        //rgb += kD * radLight * cosNl;
                        *rgb += light.radiance_.Le_ * cosNl;
                    }
                }
            }
            *rgb *= kD;
            *rgb /= samplesLight;
        } // end direct + ambient
    }

    // specular reflection
    if (::glm::any(::glm::greaterThan(kS, ::glm::vec3 {0}))) {
        const auto &reflectionDir {::glm::reflect(ray.direction_, shadingNormal)};
        const Ray &specularRay {reflectionDir, intersection.point_, rayDepth + 1, intersection.primitive_};
        ::glm::vec3 LiS_RGB {};
        rayTrace(&LiS_RGB, specularRay);
        *rgb += kS * LiS_RGB;
    }

    // specular transmission
    if (::glm::any(::glm::greaterThan(kT, ::glm::vec3 {0}))) {
        const auto refractiveIndice {1.0F / intersection.material_->refractiveIndice_};
        const auto &refractDir {::glm::refract(ray.direction_, shadingNormal, refractiveIndice)};
        const Ray &transmissionRay {refractDir, intersection.point_, rayDepth + 1, intersection.primitive_};
        ::glm::vec3 LiT_RGB {};
        rayTrace(&LiT_RGB, transmissionRay);
        *rgb += kT * LiT_RGB;
    }
    *rgb += kD *  0.1F;//ambient light
    return false;
}
