#include "MobileRT/Shader.hpp"
#include "MobileRT/Utils.hpp"
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
    const ::std::uint32_t mask {0xFFFFF};
    const ::std::uint32_t size {mask + 1};
    ::std::array<float, size> values {};

    bool fillThings() {
        for (auto it {values.begin()}; it < values.end(); ::std::advance(it, 1)) {
            const auto index {static_cast<::std::uint32_t> (::std::distance(values.begin(), it))};
            *it = ::MobileRT::haltonSequence(index, 2);
        }
        static ::std::random_device randomDevice {};
        static ::std::mt19937 generator {randomDevice()};
        ::std::shuffle(values.begin(), values.end(), generator);
        return true;
    }
}//namespace

Shader::Shader(Scene scene, const ::std::int32_t samplesLight, const Accelerator accelerator) :
    materials_ {::std::move(scene.materials_)},
    accelerator_ {accelerator},
    samplesLight_ {samplesLight} {
    static auto unused {fillThings()};
    static_cast<void> (unused);
    initializeAccelerators(::std::move(scene));
}

void Shader::initializeAccelerators(Scene scene) {
    LOG("initializeAccelerators");
    switch (this->accelerator_) {
        case Accelerator::ACC_NONE: {
            break;
        }

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
}

bool Shader::rayTrace(::glm::vec3 *rgb, const Ray &ray) {
    Intersection intersection {RayLengthMax, nullptr};
    const auto lastDist {intersection.length_};
    switch (this->accelerator_) {
        case Accelerator::ACC_NONE: {
            break;
        }

        case Accelerator::ACC_NAIVE: {
            intersection = this->naivePlanes_.trace(intersection, ray);
            intersection = this->naiveSpheres_.trace(intersection, ray);
            intersection = this->naiveTriangles_.trace(intersection, ray);
            break;
        }

        case Accelerator::ACC_REGULAR_GRID: {
            intersection = this->gridPlanes_.trace(intersection, ray);
            intersection = this->gridSpheres_.trace(intersection, ray);
            intersection = this->gridTriangles_.trace(intersection, ray);
            break;
        }

        case Accelerator::ACC_BVH: {
            intersection = this->bvhPlanes_.trace(intersection, ray);
            intersection = this->bvhSpheres_.trace(intersection, ray);
            intersection = this->bvhTriangles_.trace(intersection, ray);
            break;
        }
    }
    intersection = traceLights(intersection, ray);
    const auto matIndex {intersection.materialIndex_};
    if (matIndex >= 0) {
        intersection.material_ = &this->materials_[static_cast<::std::uint32_t> (matIndex)];
        const auto &texCoords {intersection.texCoords_};
        const auto &texture {intersection.material_->texture_};
        if (texCoords[0] >= 0 && texCoords[1] >= 0) {
            intersection.material_->Kd_ = texture.loadColor(texCoords);
        }
    };
    return intersection.length_ < lastDist && shade(rgb, intersection, ray);
}

bool Shader::shadowTrace(Intersection intersection, const Ray &ray) {
    const auto lastDist {intersection.length_};
    switch (this->accelerator_) {
        case Accelerator::ACC_NONE: {
            break;
        }

        case Accelerator::ACC_NAIVE: {
            intersection = this->naivePlanes_.shadowTrace(intersection, ray);
            intersection = this->naiveSpheres_.shadowTrace(intersection, ray);
            intersection = this->naiveTriangles_.shadowTrace(intersection, ray);
            break;
        }

        case Accelerator::ACC_REGULAR_GRID: {
            intersection = this->gridPlanes_.shadowTrace(intersection, ray);
            intersection = this->gridSpheres_.shadowTrace(intersection, ray);
            intersection = this->gridTriangles_.shadowTrace(intersection, ray);
            break;
        }

        case Accelerator::ACC_BVH: {
            intersection = this->bvhPlanes_.shadowTrace(intersection, ray);
            intersection = this->bvhSpheres_.shadowTrace(intersection, ray);
            intersection = this->bvhTriangles_.shadowTrace(intersection, ray);
            break;
        }
    }
    const auto res {intersection.length_ < lastDist};
    return res;
}

Intersection Shader::traceLights(Intersection intersection, const Ray &ray) const {
    for (const auto &light : this->lights_) {
        intersection = light->intersect(intersection, ray);
    }
    return intersection;
}

void Shader::resetSampling() {
    for (const auto &light : this->lights_) {
        light->resetSampling();
    }
}

::glm::vec3 Shader::getCosineSampleHemisphere(const ::glm::vec3 &normal) const {
    static ::std::atomic<::std::uint32_t> sampler {};
    const auto current1 {sampler.fetch_add(1, ::std::memory_order_relaxed)};
    const auto current2 {sampler.fetch_add(1, ::std::memory_order_relaxed)};

    const auto it1 {values.begin() + (current1 & mask)};
    const auto it2 {values.begin() + (current2 & mask)};

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

::std::uint32_t Shader::getLightIndex () {
    static ::std::atomic<::std::uint32_t> sampler {};
    const auto current {sampler.fetch_add(1, ::std::memory_order_relaxed)};

    const auto it {values.begin() + (current & mask)};

    const auto sizeLights {static_cast<::std::uint32_t> (this->lights_.size())};
    const auto randomNumber {*it};
    const auto chosenLight {static_cast<::std::uint32_t> (::std::floor(randomNumber * sizeLights * 0.99999F))};
    return chosenLight;
}

const ::std::vector<Plane>& Shader::getPlanes() const {
    switch (this->accelerator_) {
        case Accelerator::ACC_NONE: {
            return this->naivePlanes_.getPrimitives();
        }

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

const ::std::vector<Sphere>& Shader::getSpheres() const {
    switch (this->accelerator_) {
        case Accelerator::ACC_NONE: {
            return this->naiveSpheres_.getPrimitives();
        }

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

const ::std::vector<Triangle>& Shader::getTriangles() const {
    switch (this->accelerator_) {
        case Accelerator::ACC_NONE: {
            return this->naiveTriangles_.getPrimitives();
        }

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

const ::std::vector<::std::unique_ptr<Light>>& Shader::getLights() const {
    return this->lights_;
}

const ::std::vector<Material>& Shader::getMaterials() const {
    return this->materials_;
}
