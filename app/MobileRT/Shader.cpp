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
    const ::std::uint32_t MASK{0xFFFFF};
    const ::std::uint32_t SIZE{MASK + 1};
    ::std::array<float, SIZE> VALUES{};

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
        scene_{::std::move(scene)},
        accelerator_{accelerator},
        samplesLight_{samplesLight}
{
    static bool unused{FillThings()};
    static_cast<void> (unused);
}

void Shader::initializeAccelerators(Camera *const camera) noexcept {
    switch (accelerator_) {
        case Accelerator::NAIVE: {
            break;
        }
        case Accelerator::REGULAR_GRID: {
            ::glm::vec3 min {RayLengthMax};
            ::glm::vec3 max {-RayLengthMax};
            ::std::vector<Primitive<Triangle> *> triangles{convertVector(this->scene_.triangles_)};
            ::std::vector<Primitive<Sphere> *> spheres{convertVector(this->scene_.spheres_)};
            ::std::vector<Primitive<Plane> *> planes{convertVector(this->scene_.planes_)};
            Scene::getBounds<Primitive<Triangle>>(triangles, &min, &max);
            Scene::getBounds<Primitive<Sphere>>(spheres, &min, &max);
            Scene::getBounds<Primitive<Plane>>(planes, &min, &max);
            Scene::getBounds(::std::vector<Camera *> {camera}, &min, &max);
            const AABB sceneBounds {min - 0.01f, max + 0.01f};
            regularGrid_ = RegularGrid {sceneBounds, &scene_, 32};
            break;
        }
        case Accelerator::BVH: {
            bvhPlanes_ = ::MobileRT::BVH<MobileRT::Plane> {::std::move(scene_.planes_)};
            bvhSpheres_ = ::MobileRT::BVH<MobileRT::Sphere> {::std::move(scene_.spheres_)};
            bvhTriangles_ = ::MobileRT::BVH<MobileRT::Triangle> {::std::move(scene_.triangles_)};
            break;
        }
    }
}

Intersection Shader::traceTouch(Intersection intersection, const Ray &ray) noexcept {
    const Intersection &res{this->scene_.trace(intersection, ray)};
    return res;
}

Shader::~Shader() noexcept {
    LOG("SHADER DELETED");
}

bool Shader::shadowTrace(Intersection intersection, const Ray &ray) noexcept {
    const float lastDist {intersection.length_};
    switch (accelerator_) {
        case Accelerator::NAIVE: {
            intersection = this->scene_.shadowTrace(intersection, ray);
            break;
        }

        case Accelerator::REGULAR_GRID: {
            intersection = this->regularGrid_.shadowTrace(intersection, ray);
            break;
        }

        case Accelerator::BVH: {
            intersection = this->bvhPlanes_.shadowTrace(intersection, ray);
            intersection = this->bvhSpheres_.shadowTrace(intersection, ray);
            intersection = this->bvhTriangles_.shadowTrace(intersection, ray);
            break;
        }
    }
    const bool res{intersection.length_ < lastDist};
    return res;
}

bool Shader::rayTrace(::glm::vec3 *rgb, const Ray &ray) noexcept {
    Intersection intersection{RayLengthMax, nullptr};
    const float lastDist {intersection.length_};
    switch (accelerator_) {
        case Accelerator::NAIVE: {
            intersection = this->scene_.trace(intersection, ray);
            break;
        }

        case Accelerator::REGULAR_GRID: {
            intersection = this->regularGrid_.trace(intersection, ray);
            break;
        }

        case Accelerator::BVH: {
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
    static ::std::atomic<::std::uint32_t> sampler {0};
    const ::std::uint32_t current1 {sampler.fetch_add(1, ::std::memory_order_relaxed)};
    const ::std::uint32_t current2 {sampler.fetch_add(1, ::std::memory_order_relaxed)};

    const auto it1 {VALUES.begin() + (current1 & MASK)};
    const auto it2 {VALUES.begin() + (current2 & MASK)};

    const float uniformRandom1{*it1};
    const float uniformRandom2{*it2};

    const float phi{
            ::glm::two_pi<float>() * uniformRandom1};// random angle around - azimuthal angle
    const float r2{uniformRandom2};// random distance from center
    const float cosTheta{::std::sqrt(
            r2)};// square root of distance from center - cos(theta) = cos(elevation angle)

    ::glm::vec3 u{::std::abs(normal[0]) > 0.1f ? ::glm::vec3 {0.0f, 1.0f, 0.0f} :
                  ::glm::vec3 {1.0f, 0.0f, 0.0f}};
    u = ::glm::normalize(::glm::cross(u, normal));// second axis
    const ::glm::vec3 &v{::glm::cross(normal, u)};// final axis

    ::glm::vec3 direction{u * (::std::cos(phi) * cosTheta) +
                          v * (::std::sin(phi) * cosTheta) +
                          normal * ::std::sqrt(1.0f - r2)};
    direction = ::glm::normalize(direction);

    /*float phi2 {::std::acos(::std::sqrt(1.0f - uniformRandom1))};
    float theta2 = ::glm::two_pi<float>() * r2;
    ::glm::vec3 dir {sin(phi2) * cos(theta2),
                    cos(phi2),
                     sin(phi2) * sin(theta2)};*/

    return direction;
}

::std::uint32_t Shader::getLightIndex () {
    static ::std::atomic<::std::uint32_t> sampler {0};
    const ::std::uint32_t current {sampler.fetch_add(1, ::std::memory_order_relaxed)};

    const auto it {VALUES.begin() + (current & MASK)};

    const ::std::uint32_t sizeLights {static_cast<::std::uint32_t>(scene_.lights_.size())};
    const float randomNumber {*it};
    const ::std::uint32_t chosenLight {
        static_cast<::std::uint32_t> (::std::floor(randomNumber * sizeLights * 0.99999f))};
    return chosenLight;
}
