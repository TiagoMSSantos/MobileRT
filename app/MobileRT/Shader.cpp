#include "MobileRT/Shader.hpp"
#include "MobileRT/Utils.hpp"
#include <array>
#include <glm/glm.hpp>
#include <glm/gtc/constants.hpp>
#include <random>
#include <utility>

using ::MobileRT::BVH;
using ::MobileRT::Camera;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Primitive;
using ::MobileRT::Shader;

namespace {
    const ::std::uint32_t MASK {0xFFFFF};
    const ::std::uint32_t SIZE {MASK + 1};
    ::std::array<float, SIZE> VALUES {};

    bool FillThings() {
        for (auto it {VALUES.begin()}; it < VALUES.end(); std::advance(it, 1)) {
            const ::std::uint32_t index {static_cast<uint32_t>(::std::distance(VALUES.begin(), it))};
            *it = ::MobileRT::haltonSequence(index, 2);
        }
        static ::std::random_device randomDevice {"/dev/urandom"};
        static ::std::mt19937 generator {randomDevice()};
        ::std::shuffle(VALUES.begin(), VALUES.end(), generator);
        return true;
    }
}//namespace

Shader::Shader(
    Scene scene,
    const ::std::uint32_t samplesLight,
    const Accelerator accelerator) noexcept :
        scene_ {::std::move(scene)},
        accelerator_ {accelerator},
        samplesLight_ {samplesLight} {
    static bool unused{FillThings()};
    static_cast<void> (unused);
}

void Shader::initializeAccelerators(Camera *const camera) noexcept {
    switch (accelerator_) {
        case Accelerator::ACC_NAIVE: {
            break;
        }

        case Accelerator::ACC_REGULAR_GRID: {
            ::glm::vec3 minPlanes {RayLengthMax};
            ::glm::vec3 maxPlanes {-RayLengthMax};
            ::glm::vec3 minSpheres {RayLengthMax};
            ::glm::vec3 maxSpheres {-RayLengthMax};
            ::glm::vec3 minTriangles {RayLengthMax};
            ::glm::vec3 maxTriangles {-RayLengthMax};

            ::std::vector<Primitive<Plane> *> planes {convertVector(this->scene_.planes_)};
            ::std::vector<Primitive<Sphere> *> spheres {convertVector(this->scene_.spheres_)};
            ::std::vector<Primitive<Triangle> *> triangles {convertVector(this->scene_.triangles_)};

            Scene::getBounds<Primitive<Plane>> (planes, &minPlanes, &maxPlanes);
            Scene::getBounds<Primitive<Sphere>> (spheres, &minSpheres, &maxSpheres);
            Scene::getBounds<Primitive<Triangle>> (triangles, &minTriangles, &maxTriangles);

            Scene::getBounds(::std::vector<Camera *> {camera}, &minPlanes, &maxPlanes);
            Scene::getBounds(::std::vector<Camera *> {camera}, &minSpheres, &maxSpheres);
            Scene::getBounds(::std::vector<Camera *> {camera}, &minTriangles, &maxTriangles);

            const AABB sceneBoundsPlanes {minPlanes - 0.01F, maxPlanes + 0.01F};
            const AABB sceneBoundsSpheres {minSpheres - 0.01F, maxSpheres + 0.01F};
            const AABB sceneBoundsTriangles {minTriangles - 0.01F, maxTriangles + 0.01F};

            gridPlanes_ = ::MobileRT::RegularGrid<::MobileRT::Plane> {
                    sceneBoundsPlanes, 32, ::std::move(scene_.planes_)
            };
            gridSpheres_ = ::MobileRT::RegularGrid<::MobileRT::Sphere> {
                    sceneBoundsSpheres, 32, ::std::move(scene_.spheres_)
            };
            gridTriangles_ = ::MobileRT::RegularGrid<::MobileRT::Triangle> {
                    sceneBoundsTriangles, 32, ::std::move(scene_.triangles_)
            };
            break;
        }

        case Accelerator::ACC_BVH: {
            bvhPlanes_ = ::MobileRT::BVH<::MobileRT::Plane> {::std::move(scene_.planes_)};
            bvhSpheres_ = ::MobileRT::BVH<::MobileRT::Sphere> {::std::move(scene_.spheres_)};
            bvhTriangles_ = ::MobileRT::BVH<::MobileRT::Triangle> {::std::move(scene_.triangles_)};
            break;
        }
    }
}

Shader::~Shader() noexcept {
    LOG("SHADER DELETED");
}

bool Shader::shadowTrace(Intersection intersection, const Ray &ray) noexcept {
    const float lastDist {intersection.length_};
    switch (this->accelerator_) {
        case Accelerator::ACC_NAIVE: {
            intersection = this->scene_.shadowTrace(intersection, ray);
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
    const bool res {intersection.length_ < lastDist};
    return res;
}

bool Shader::rayTrace(::glm::vec3 *rgb, const Ray &ray) noexcept {
    Intersection intersection{RayLengthMax, nullptr};
    const float lastDist {intersection.length_};
    switch (this->accelerator_) {
        case Accelerator::ACC_NAIVE: {
            intersection = this->scene_.trace(intersection, ray);
            break;
        }

        case Accelerator::ACC_REGULAR_GRID: {
            intersection = this->gridPlanes_.trace(intersection, ray);
            intersection = this->gridSpheres_.trace(intersection, ray);
            intersection = this->gridTriangles_.trace(intersection, ray);
            intersection = this->scene_.traceLights(intersection, ray);
            break;
        }

        case Accelerator::ACC_BVH: {
            intersection = this->bvhPlanes_.trace(intersection, ray);
            intersection = this->bvhSpheres_.trace(intersection, ray);
            intersection = this->bvhTriangles_.trace(intersection, ray);
            intersection = this->scene_.traceLights(intersection, ray);
            break;
        }
    }
    const bool res{intersection.length_ < lastDist && shade(rgb, intersection, ray)};
    return res;
}

void Shader::resetSampling() noexcept {
    this->scene_.resetSampling();
}

::glm::vec3 Shader::getCosineSampleHemisphere(const ::glm::vec3 &normal) const noexcept {
    static ::std::atomic<::std::uint32_t> sampler {};
    const ::std::uint32_t current1 {sampler.fetch_add(1, ::std::memory_order_relaxed)};
    const ::std::uint32_t current2 {sampler.fetch_add(1, ::std::memory_order_relaxed)};

    const auto it1 {VALUES.begin() + (current1 & MASK)};
    const auto it2 {VALUES.begin() + (current2 & MASK)};

    const float uniformRandom1 {*it1};
    const float uniformRandom2 {*it2};

    const float phi {::glm::two_pi<float>() * uniformRandom1};// random angle around - azimuthal angle
    const float r2 {uniformRandom2};// random distance from center
    const float cosTheta {::std::sqrt(r2)};// square root of distance from center - cos(theta) = cos(elevation angle)

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

    /*float phi2 {::std::acos(::std::sqrt(1.0F - uniformRandom1))};
    float theta2 = ::glm::two_pi<float>() * r2;
    ::glm::vec3 dir {sin(phi2) * cos(theta2),
                    cos(phi2),
                     sin(phi2) * sin(theta2)};*/

    return direction;
}

::std::uint32_t Shader::getLightIndex () {
    static ::std::atomic<::std::uint32_t> sampler {};
    const ::std::uint32_t current {sampler.fetch_add(1, ::std::memory_order_relaxed)};

    const auto it {VALUES.begin() + (current & MASK)};

    const ::std::uint32_t sizeLights {static_cast<::std::uint32_t>(scene_.lights_.size())};
    const float randomNumber {*it};
    const ::std::uint32_t chosenLight {
        static_cast<::std::uint32_t> (::std::floor(randomNumber * sizeLights * 0.99999F))};
    return chosenLight;
}
