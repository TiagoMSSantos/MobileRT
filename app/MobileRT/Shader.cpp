#include "MobileRT/Shader.hpp"
#include "MobileRT/Utils/Utils.hpp"
#include <array>
#include <glm/glm.hpp>
#include <glm/gtc/constants.hpp>
#include <random>
#include <utility>

using ::MobileRT::BVH;
using ::MobileRT::RegularGrid;
using ::MobileRT::Naive;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Shader;
using ::MobileRT::Plane;
using ::MobileRT::Sphere;
using ::MobileRT::Triangle;
using ::MobileRT::Light;
using ::MobileRT::Material;

namespace {
    ::std::array<float, ::MobileRT::ArraySize> randomSequence {};
}//namespace

/**
 * The constructor.
 *
 * @param scene        The scene.
 * @param samplesLight The number of samples per light.
 * @param accelerator  The acceleration structure to use.
 */
Shader::Shader(Scene scene, const ::std::int32_t samplesLight, const Accelerator accelerator) :
    materials_ {::std::move(scene.materials_)},
    accelerator_ {accelerator},
    samplesLight_ {samplesLight} {
    fillArrayWithHaltonSeq(&randomSequence);
    initializeAccelerators(::std::move(scene));
}

/**
 * Puts all the primitives of the scene into an acceleration structure.
 *
 * @param scene The scene geometry.
 */
void Shader::initializeAccelerators(Scene scene) {
    LOG_DEBUG("initializeAccelerators");
    switch (this->accelerator_) {
        case Accelerator::ACC_NAIVE: {
            this->naivePlanes_ = Naive<Plane> {::std::move(scene.planes_)};
            this->naiveSpheres_ = Naive<Sphere> {::std::move(scene.spheres_)};
            this->naiveTriangles_ = Naive<Triangle> {::std::move(scene.triangles_)};
            break;
        }

        case Accelerator::ACC_REGULAR_GRID: {
            const auto gridSize {32U};
            this->gridPlanes_ = RegularGrid<Plane> {::std::move(scene.planes_), gridSize};
            this->gridSpheres_ = RegularGrid<Sphere> {::std::move(scene.spheres_), gridSize};
            this->gridTriangles_ = RegularGrid<Triangle> {::std::move(scene.triangles_), gridSize};
            break;
        }

        case Accelerator::ACC_BVH: {
            this->bvhPlanes_ = BVH<Plane> {::std::move(scene.planes_)};
            this->bvhSpheres_ = BVH<Sphere> {::std::move(scene.spheres_)};
            this->bvhTriangles_ = BVH<Triangle> {::std::move(scene.triangles_)};
            break;
        }
    }
    this->lights_ = ::std::move(scene.lights_);
    LOG_DEBUG("accelerator = ", this->accelerator_);
    LOG_DEBUG("materials = ", this->materials_.size());
    LOG_DEBUG("lights = ", this->lights_.size());
}

/**
 * Determines if a casted ray intersects a light source in the scene or not.
 *
 * @param rgb A pointer where the color value of the pixel should be put.
 * @param ray The casted ray into the scene.
 * @return Whether the casted ray intersects a light source in the scene or not.
 */
bool Shader::rayTrace(::glm::vec3 *rgb, const Ray &ray) {
    Intersection intersection {ray};
    const auto lastDist {intersection.length_};
    switch (this->accelerator_) {
        case Accelerator::ACC_NAIVE: {
            intersection = this->naivePlanes_.trace(intersection);
            intersection = this->naiveSpheres_.trace(intersection);
            intersection = this->naiveTriangles_.trace(intersection);
            break;
        }

        case Accelerator::ACC_REGULAR_GRID: {
            intersection = this->gridPlanes_.trace(intersection);
            intersection = this->gridSpheres_.trace(intersection);
            intersection = this->gridTriangles_.trace(intersection);
            break;
        }

        case Accelerator::ACC_BVH: {
            intersection = this->bvhPlanes_.trace(intersection);
            intersection = this->bvhSpheres_.trace(intersection);
            intersection = this->bvhTriangles_.trace(intersection);
            break;
        }
    }
    intersection = traceLights(intersection);
    const auto matIndex {intersection.materialIndex_};
    if (matIndex >= 0) {
        auto &material {this->materials_[static_cast<::std::uint32_t> (matIndex)]};
        intersection.material_ = &material;
        const auto &texCoords {intersection.texCoords_};
        if (texCoords[0] >= 0 && texCoords[1] >= 0) {
            const auto &texture {material.texture_};
            intersection.material_->Kd_ = texture.loadColor(texCoords);
        }
    }
    return intersection.length_ < lastDist && shade(rgb, intersection);
}

/**
 * Determines if a casted ray intersects a primitive in the scene between the origin of the ray and a light source.
 *
 * @param distance The distance from the origin of the ray to the light source.
 * @param ray      The casted ray.
 * @return Whether the casted ray intersects a primitive in the scene or not.
 */
bool Shader::shadowTrace(const float distance, const Ray &ray) {
    Intersection intersection {ray, distance};
    switch (this->accelerator_) {
        case Accelerator::ACC_NAIVE: {
            intersection = this->naivePlanes_.shadowTrace(intersection);
            intersection = this->naiveSpheres_.shadowTrace(intersection);
            intersection = this->naiveTriangles_.shadowTrace(intersection);
            break;
        }

        case Accelerator::ACC_REGULAR_GRID: {
            intersection = this->gridPlanes_.shadowTrace(intersection);
            intersection = this->gridSpheres_.shadowTrace(intersection);
            intersection = this->gridTriangles_.shadowTrace(intersection);
            break;
        }

        case Accelerator::ACC_BVH: {
            intersection = this->bvhPlanes_.shadowTrace(intersection);
            intersection = this->bvhSpheres_.shadowTrace(intersection);
            intersection = this->bvhTriangles_.shadowTrace(intersection);
            break;
        }
    }
    const auto res {intersection.length_ < distance};
    return res;
}

/**
 * Helper method which calculates the nearest intersection point of a casted ray and the light sources.
 *
 * @param intersection The current intersection of the ray with previous primitives.
 * @return The intersection of the casted ray and the light sources.
 */
Intersection Shader::traceLights(Intersection intersection) const {
    for (const auto &light : this->lights_) {
        intersection = light->intersect(intersection);
    }
    return intersection;
}

/**
 * Resets the sampling process of all the lights in the scene.
 */
void Shader::resetSampling() {
    for (const auto &light : this->lights_) {
        light->resetSampling();
    }
}

/**
 * Helper method which generates a random 3D direction in a hemisphere in world coordinates.
 *
 * @param normal The normal of the hemisphere.
 * @return A random direction in a hemisphere.
 */
::glm::vec3 Shader::getCosineSampleHemisphere(const ::glm::vec3 &normal) const {
    static ::std::atomic<::std::uint32_t> sampler {};
    const auto current1 {sampler.fetch_add(1, ::std::memory_order_relaxed)};
    const auto current2 {sampler.fetch_add(1, ::std::memory_order_relaxed)};

    const auto it1 {randomSequence.begin() + (current1 & ::MobileRT::ArrayMask)};
    const auto it2 {randomSequence.begin() + (current2 & ::MobileRT::ArrayMask)};

    const auto uniformRandom1 {*it1};
    const auto uniformRandom2 {*it2};

    const auto phi {::glm::two_pi<float> () * uniformRandom1};// random angle around - azimuthal angle
    const auto r2 {uniformRandom2};// random distance from center
    const auto cosTheta {::std::sqrt(r2)};// square root of distance from center - cos(theta) = cos(elevation angle)

    ::glm::vec3 u {::std::abs(normal[0]) > 0.1F
        ? ::glm::vec3 {0.0F, 1.0F, 0.0F}
        : ::glm::vec3 {1.0F, 0.0F, 0.0F}
    };
    u = ::glm::normalize(::glm::cross(u, normal));// second axis
    const ::glm::vec3 &v {::glm::cross(normal, u)};// final axis

    ::glm::vec3 direction {u * (::std::cos(phi) * cosTheta) +
                           v * (::std::sin(phi) * cosTheta) +
                           normal * ::std::sqrt(1.0F - r2)};
    direction = ::glm::normalize(direction);

    return direction;
}

/**
 * Calculates the index of a random chosen light in the scene.
 *
 * @return The index of a random chosen light.
 */
::std::uint32_t Shader::getLightIndex () {
    static ::std::atomic<::std::uint32_t> sampler {};
    const auto current {sampler.fetch_add(1, ::std::memory_order_relaxed)};

    const auto it {randomSequence.begin() + (current & ::MobileRT::ArrayMask)};

    const auto sizeLights {static_cast<::std::uint32_t> (this->lights_.size())};
    const auto randomNumber {*it};
    const auto chosenLight {static_cast<::std::uint32_t> (::std::floor(randomNumber * sizeLights * 0.99999F))};
    return chosenLight;
}

/**
 * Gets the planes in the scene.
 *
 * @return The planes in the scene.
 */
const ::std::vector<Plane>& Shader::getPlanes() const {
    switch (this->accelerator_) {
        case Accelerator::ACC_NAIVE: {
            return this->naivePlanes_.getPrimitives();
        }

        case Accelerator::ACC_REGULAR_GRID: {
            return this->gridPlanes_.getPrimitives();
        }

        case Accelerator::ACC_BVH: {
            return this->bvhPlanes_.getPrimitives();
        }
    }
    return this->naivePlanes_.getPrimitives();
}

/**
 * Gets the spheres in the scene.
 *
 * @return The spheres in the scene.
 */
const ::std::vector<Sphere>& Shader::getSpheres() const {
    switch (this->accelerator_) {
        case Accelerator::ACC_NAIVE: {
            return this->naiveSpheres_.getPrimitives();
        }

        case Accelerator::ACC_REGULAR_GRID: {
            return this->gridSpheres_.getPrimitives();
        }

        case Accelerator::ACC_BVH: {
            return this->bvhSpheres_.getPrimitives();
        }
    }
    return this->naiveSpheres_.getPrimitives();
}

/**
 * Gets the triangles in the scene.
 *
 * @return The triangles in the scene.
 */
const ::std::vector<Triangle>& Shader::getTriangles() const {
    switch (this->accelerator_) {
        case Accelerator::ACC_NAIVE: {
            return this->naiveTriangles_.getPrimitives();
        }

        case Accelerator::ACC_REGULAR_GRID: {
            return this->gridTriangles_.getPrimitives();
        }

        case Accelerator::ACC_BVH: {
            return this->bvhTriangles_.getPrimitives();
        }
    }
    return this->naiveTriangles_.getPrimitives();
}

/**
 * Gets the lights in the scene.
 *
 * @return The lights in the scene.
 */
const ::std::vector<::std::unique_ptr<Light>>& Shader::getLights() const {
    return this->lights_;
}

/**
 * Gets the materials in the scene.
 *
 * @return The materials in the scene.
 */
const ::std::vector<Material>& Shader::getMaterials() const {
    return this->materials_;
}
