#include "Components/Shaders/PathTracer.hpp"
#include <glm/gtc/constants.hpp>

using ::Components::PathTracer;
using ::MobileRT::Sampler;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Scene;
using ::MobileRT::RayDepthMin;
using ::MobileRT::RayDepthMax;

PathTracer::PathTracer(Scene scene,
                       ::std::unique_ptr<Sampler> samplerRussianRoulette,
                       const ::std::int32_t samplesLight,
                       const Accelerator accelerator) :
    Shader {::std::move(scene), samplesLight, accelerator},
    samplerRussianRoulette_ {::std::move(samplerRussianRoulette)} {
    LOG_DEBUG("samplesLight = ", this->samplesLight_);
}

//pag 28 slides Monte Carlo
bool PathTracer::shade(::glm::vec3 *const rgb, const Intersection &intersection) {
    const ::std::int32_t rayDepth {intersection.ray_.depth_};
    if (rayDepth > RayDepthMax) {
        return false;
    }

    const ::glm::vec3 &lE {intersection.material_->Le_};
    //stop if it intersects a light source
    if (::MobileRT::hasPositiveValue(lE)) {
        *rgb = lE;
        return true;
    }
    ::glm::vec3 Ld {};
    ::glm::vec3 LiD {};
    ::glm::vec3 LiS {};
    ::glm::vec3 LiT {};

    const ::glm::vec3 &kD {intersection.material_->Kd_};
    const ::glm::vec3 &kS {intersection.material_->Ks_};
    const ::glm::vec3 &kT {intersection.material_->Kt_};
    const float finishProbability {0.5F};
    const float continueProbability {1.0F - finishProbability};

    // the normal always points to outside objects (e.g., spheres)
    // if the cosine between the ray and the normal is less than 0 then
    // the ray intersected the object from the inside and the shading normal
    // should be symmetric to the geometric normal
    const ::glm::vec3 &shadingNormal {intersection.normal_};

    bool intersectedLight {false};

    // shadowed direct lighting - only for diffuse materials
    //Ld = Ld (p->Wr)
    if (::MobileRT::hasPositiveValue(kD)) {
        const long long unsigned sizeLights {this->lights_.size()};
        if (sizeLights > 0) {
            const ::std::int32_t samplesLight {this->samplesLight_};
            //direct light
            for (::std::int32_t i {}; i < samplesLight; ++i) {
                //PDF = 1 / sizeLights
                const ::std::uint32_t chosenLight {getLightIndex()};
                ::MobileRT::Light &light {*this->lights_[chosenLight]};
                //calculates vector starting in intersection to the light
                const ::glm::vec3 lightPosition {light.getPosition()};
                ::glm::vec3 vectorToLight {lightPosition - intersection.point_};
                //distance from intersection to the light (and normalize it)
                const float distanceToLight {::glm::length(vectorToLight)};
                vectorToLight = ::glm::normalize(vectorToLight);
                //x*x + y*y + z*z
                const float cosNormalLight {::glm::dot(shadingNormal, vectorToLight)};
                if (cosNormalLight > 0.0F) {
                    //shadow ray->orig=intersection, dir=light
                    Ray shadowRay {vectorToLight, intersection.point_, rayDepth + 1, true, intersection.primitive_};
                    //intersection between shadow ray and the closest primitive
                    //if there are no primitives between intersection and the light
                    if (!shadowTrace(distanceToLight, ::std::move(shadowRay))) {
                        //Ld += kD * radLight * cosNormalLight * sizeLights / samplesLight
                        Ld += light.radiance_.Le_ * cosNormalLight;
                    }
                }
            }
            Ld *= kD;
            // "Ld *= sizeLights;"
            Ld /= samplesLight;
        }

        //indirect light
        if (rayDepth <= RayDepthMin || this->samplerRussianRoulette_->getSample() > finishProbability) {
            const ::glm::vec3 &newDirection {getCosineSampleHemisphere(shadingNormal)};
            Ray normalizedSecundaryRay {newDirection, intersection.point_, rayDepth + 1, false, intersection.primitive_};

            //Li = Pi/N * SOMATORIO i=1->i=N [fr (p,Wi <-> Wr) L(p <- Wi)]
            //estimator = <F^N>=1/N * ∑(i=0)(N−1) f(Xi) / pdf(Xi)

            ::glm::vec3 LiD_RGB {};
            intersectedLight = rayTrace(&LiD_RGB, ::std::move(normalizedSecundaryRay));
            //PDF = cos(theta) / Pi
            //cos (theta) = cos(dir, normal)
            //PDF = cos(dir, normal) / Pi

            //LiD += kD * LiD_RGB * cos (dir, normal) / (PDF * continueProbability)
            //LiD += kD * LiD_RGB * Pi / continueProbability
            LiD += kD * LiD_RGB;
            if (rayDepth > RayDepthMin) {
                LiD /= continueProbability * 0.5F;
            }

            //if it has Ld and if LiD intersects a light source then LiD = 0
            if (::MobileRT::hasPositiveValue(Ld) && intersectedLight) {
                LiD = {};
            }
        }
    }

    // specular reflection
    if (::MobileRT::hasPositiveValue(kS)) {
        //PDF = 1 / 2 Pi
        const ::glm::vec3 &reflectionDir {::glm::reflect(intersection.ray_.direction_, shadingNormal)};
        Ray specularRay {reflectionDir, intersection.point_, rayDepth + 1, false, intersection.primitive_};
        ::glm::vec3 LiS_RGB {};
        rayTrace(&LiS_RGB, ::std::move(specularRay));
        LiS += kS * LiS_RGB;
    }

    // specular transmission
    if (::MobileRT::hasPositiveValue(kT)) {
        //PDF = 1 / 2 Pi
        const float refractiveIndice {1.0F / intersection.material_->refractiveIndice_};
        const ::glm::vec3 &refractDir {::glm::refract(intersection.ray_.direction_, shadingNormal, refractiveIndice)};
        Ray transmissionRay {refractDir, intersection.point_, rayDepth + 1, false, intersection.primitive_};
        ::glm::vec3 LiT_RGB {};
        rayTrace(&LiT_RGB, ::std::move(transmissionRay));
        LiT += kT * LiT_RGB;
    }

    *rgb += Ld;
    *rgb += LiD;
    *rgb += LiS;
    *rgb += LiT;
    return intersectedLight;
}

void PathTracer::resetSampling() {
    Shader::resetSampling();
    this->samplerRussianRoulette_->resetSampling();
}
