//
// Created by puscas on 20-02-2017.
//

#include "Components/Shaders/PathTracer.hpp"
#include <glm/gtc/constants.hpp>

using ::Components::PathTracer;
using ::MobileRT::Light;
using ::MobileRT::Sampler;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Scene;

PathTracer::PathTracer(Scene scene,
                       ::std::unique_ptr<Sampler> samplerRussianRoulette,
                       const ::std::uint32_t samplesLight,
                       const Accelerator accelerator) noexcept :
        Shader{::std::move(scene), samplesLight, accelerator},
        samplerRussianRoulette_{::std::move(samplerRussianRoulette)} {
    LOG("samplesLight = ", this->samplesLight_);
}

//pag 28 slides Monte Carlo
bool PathTracer::shade(
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
    ::glm::vec3 Ld {};
    ::glm::vec3 LiD {};
    ::glm::vec3 LiS {};
    ::glm::vec3 LiT {};

    const ::glm::vec3 &kD {intersection.material_->Kd_};
    const ::glm::vec3 &kS {intersection.material_->Ks_};
    const ::glm::vec3 &kT {intersection.material_->Kt_};
    const float finish_probability {0.5f};
    const float continue_probability {1.0f - finish_probability};

    // the normal always points to outside objects (e.g., spheres)
    // if the cosine between the ray and the normal is less than 0 then
    // the ray intersected the object from the inside and the shading normal
    // should be symmetric to the geometric normal
    const ::glm::vec3 &shadingNormal{intersection.normal_};

    bool intersectedLight {false};

    // shadowed direct lighting - only for diffuse materials
    //Ld = Ld (p->Wr)
    if (::glm::any(::glm::greaterThan(kD, ::glm::vec3 {0}))) {
        const ::std::uint32_t sizeLights {
            static_cast<::std::uint32_t>(scene_.lights_.size())};
        if (sizeLights > 0) {
            const ::std::uint32_t samplesLight {this->samplesLight_};
            //direct light
            for (::std::uint32_t i {0}; i < samplesLight; ++i) {
                //PDF = 1 / sizeLights
                const ::std::uint32_t chosenLight {getLightIndex()};
                Light &light(*scene_.lights_[chosenLight]);
                //calculates vector starting in intersection to the light
                const ::glm::vec3 lightPosition {light.getPosition()};
                ::glm::vec3 vectorToLight {lightPosition - intersection.point_};
                //distance from intersection to the light (and normalize it)
                const float distanceToLight {::glm::length(vectorToLight)};
                vectorToLight = ::glm::normalize(vectorToLight);
                //x*x + y*y + z*z
                const float cosNormalLight {::glm::dot(shadingNormal, vectorToLight)};
                if (cosNormalLight > 0.0f) {
                    //shadow ray->orig=intersection, dir=light
                    const Ray shadowRay {
                        vectorToLight, intersection.point_, rayDepth + 1, intersection.primitive_};
                    //intersection between shadow ray and the closest primitive
                    //if there are no primitives between intersection and the light
                    Intersection intersectLight {distanceToLight, intersection.primitive_};
                    if (!shadowTrace(intersectLight, shadowRay)) {
                        //Ld += kD * radLight * cosNormalLight * sizeLights / samplesLight
                        Ld += light.radiance_.Le_ * cosNormalLight;
                    }
                }
            }
            Ld *= kD;
            //Ld *= sizeLights;
            Ld /= samplesLight;
        }

        //indirect light
        if (rayDepth <= ::MobileRT::RayDepthMin ||
            samplerRussianRoulette_->getSample() > finish_probability) {
            const ::glm::vec3 &newDirection {getCosineSampleHemisphere(shadingNormal)};
            const Ray normalizedSecundaryRay {newDirection, intersection.point_, rayDepth + 1,
                                       intersection.primitive_};

            //Li = Pi/N * SOMATORIO i=1->i=N [fr (p,Wi <-> Wr) L(p <- Wi)]
            //estimator = <F^N>=1/N * ∑(i=0)(N−1) f(Xi) / pdf(Xi)

            ::glm::vec3 LiD_RGB {};
            intersectedLight = rayTrace(&LiD_RGB, normalizedSecundaryRay);
            //PDF = cos(theta) / Pi
            //cos (theta) = cos(dir, normal)
            //PDF = cos(dir, normal) / Pi

            //LiD += kD * LiD_RGB * cos (dir, normal) / (PDF * continue_probability)
            //LiD += kD * LiD_RGB * Pi / continue_probability
            LiD += kD * LiD_RGB;
            if (rayDepth > ::MobileRT::RayDepthMin) {
                LiD /= continue_probability * 0.5f;
            }

            //if it has Ld and if LiD intersects a light source then LiD = 0
            if (::glm::any(::glm::greaterThan(Ld, ::glm::vec3 {0})) && intersectedLight) {
                LiD = {};
            }
        }
    }

    // specular reflection
    if (::glm::any(::glm::greaterThan(kS, ::glm::vec3 {0}))) {
        //PDF = 1 / 2 Pi
        const ::glm::vec3 &reflectionDir {
            ::glm::reflect(ray.direction_, shadingNormal)};
        const Ray specularRay {
            reflectionDir, intersection.point_, rayDepth + 1, intersection.primitive_};
        ::glm::vec3 LiS_RGB {};
        rayTrace(&LiS_RGB, specularRay);
        LiS += kS * LiS_RGB;
    }

    // specular transmission
    if (::glm::any(::glm::greaterThan(kT, ::glm::vec3 {0}))) {
        //PDF = 1 / 2 Pi
        const float refractiveIndice {1.0f / intersection.material_->refractiveIndice_};
        const ::glm::vec3 &refractDir {
            ::glm::refract(ray.direction_, shadingNormal, refractiveIndice)};
        const Ray transmissionRay {
            refractDir, intersection.point_, rayDepth + 1, intersection.primitive_};
        ::glm::vec3 LiT_RGB {};
        rayTrace(&LiT_RGB, transmissionRay);
        LiT += kT * LiT_RGB;
    }

    *rgb += Ld;
    *rgb += LiD;
    *rgb += LiS;
    *rgb += LiT;
    return intersectedLight;
}

void PathTracer::resetSampling() noexcept {
    Shader::resetSampling();
    this->samplerRussianRoulette_->resetSampling();
}
