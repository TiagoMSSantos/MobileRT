#include "Components/Shaders/Whitted.hpp"

using ::Components::Whitted;
using ::MobileRT::Light;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Scene;

Whitted::Whitted(Scene scene, const ::std::uint32_t samplesLight, Accelerator accelerator) noexcept :
        Shader{::std::move(scene), samplesLight, accelerator} {
}

bool Whitted::shade(
    ::glm::vec3 *const rgb, const Intersection &intersection, const Ray &ray) noexcept {

    const ::std::int32_t rayDepth {ray.depth_};
    if (rayDepth > ::MobileRT::RayDepthMax) {
        return false;
    }

    const ::glm::vec3 &Le{intersection.material_->Le_};
    //stop if it intersects a light source
    if (::glm::any(::glm::greaterThan(Le, ::glm::vec3 {0}))) {
        *rgb = Le;
        return true;
    }

    const ::glm::vec3 &kD {intersection.material_->Kd_};
    const ::glm::vec3 &kS {intersection.material_->Ks_};
    const ::glm::vec3 &kT {intersection.material_->Kt_};

    // the normal always points to outside objects (e.g., spheres)
    // if the cosine between the ray and the normal is less than 0 then
    // the ray intersected the object from the inside and the shading normal
    // should be symmetric to the geometric normal
    const ::glm::vec3 &shadingNormal{intersection.normal_};

    // shadowed direct lighting - only for diffuse materials
    if (::glm::any(::glm::greaterThan(kD, ::glm::vec3 {0}))) {
        const ::std::uint32_t sizeLights {
            static_cast<::std::uint32_t>(scene_.lights_.size())};
        if (sizeLights > 0) {
            const ::std::uint32_t samplesLight {this->samplesLight_};
            for (::std::uint32_t i {0}; i < samplesLight; ++i) {
                const ::std::uint32_t chosenLight {getLightIndex()};
                Light &light(*scene_.lights_[chosenLight]);
                const ::glm::vec3 lightPosition {light.getPosition()};
                //calculates vector starting in intersection to the light
                ::glm::vec3 vectorToLight {lightPosition - intersection.point_};
                //distance from intersection to the light (and normalize it)
                const float distanceToLight {::glm::length(vectorToLight)};
                vectorToLight = ::glm::normalize(vectorToLight);
                const float cos_N_L {::glm::dot(shadingNormal, vectorToLight)};
                if (cos_N_L > 0.0f) {
                    //shadow ray - orig=intersection, dir=light
                    const Ray shadowRay {vectorToLight, intersection.point_, rayDepth + 1,
                                    intersection.primitive_};
                    Intersection lightIntersection {distanceToLight, intersection.primitive_};
                    //intersection between shadow ray and the closest primitive
                    //if there are no primitives between intersection and the light
                    if (!shadowTrace(lightIntersection, shadowRay)) {
                        //rgb += kD * radLight * cos_N_L;
                        *rgb += light.radiance_.Le_ * cos_N_L;
                    }
                }
            }
            *rgb *= kD;
            *rgb /= samplesLight;
        } // end direct + ambient
    }

    // specular reflection
    if (::glm::any(::glm::greaterThan(kS, ::glm::vec3 {0}))) {
        const ::glm::vec3 &reflectionDir {
            ::glm::reflect(ray.direction_, shadingNormal)};
        const Ray specularRay {
            reflectionDir, intersection.point_, rayDepth + 1, intersection.primitive_};
        ::glm::vec3 LiS_RGB {};
        rayTrace(&LiS_RGB, specularRay);
        *rgb += kS * LiS_RGB;
    }

    // specular transmission
    if (::glm::any(::glm::greaterThan(kT, ::glm::vec3 {0}))) {
        const float refractiveIndice {1.0f / intersection.material_->refractiveIndice_};
        const ::glm::vec3 &refractDir {
            ::glm::refract(ray.direction_, shadingNormal, refractiveIndice)};
        const Ray transmissionRay {
            refractDir, intersection.point_, rayDepth + 1, intersection.primitive_};
        ::glm::vec3 LiT_RGB {};
        rayTrace(&LiT_RGB, transmissionRay);
        *rgb += kT * LiT_RGB;
    }
    *rgb += kD *  0.1f;//ambient light
    return false;
}
