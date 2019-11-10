#include "MobileRT/Scene.hpp"

using ::MobileRT::Scene;
using ::MobileRT::Triangle;
using ::MobileRT::Sphere;
using ::MobileRT::Plane;
using ::MobileRT::Intersection;

Scene::~Scene() noexcept {
    //may not free the memory
    this->triangles_.clear();
    this->spheres_.clear();
    this->planes_.clear();
    this->lights_.clear();

    //force free memory
    ::std::vector<MobileRT::Primitive<MobileRT::Plane>>{}.swap(planes_);
    ::std::vector<MobileRT::Primitive<MobileRT::Sphere>>{}.swap(spheres_);
    ::std::vector<MobileRT::Primitive<MobileRT::Triangle>>{}.swap(triangles_);
    ::std::vector<::std::unique_ptr<Light>>{}.swap(lights_);

    LOG("SCENE DELETED");
}

Intersection Scene::traceLights(Intersection intersection, const Ray &ray) const noexcept {
    const ::std::uint32_t lightsSize {static_cast<::std::uint32_t> (lights_.size())};
    for (::std::uint32_t i {0}; i < lightsSize; ++i) {
        const Light &light{*this->lights_[static_cast<::std::uint32_t> (i)]};
        intersection = light.intersect(intersection, ray);
    }

    return intersection;
}

template<typename T>
Intersection Scene::trace(::std::vector<T> &primitives, Intersection intersection,
                  const Ray &ray) noexcept {
    for (T &primitive : primitives) {
        intersection = primitive.intersect(intersection, ray);
    }
    return intersection;
}

Intersection Scene::trace(Intersection intersection, const Ray &ray) noexcept {
    intersection =
        trace<::MobileRT::Primitive<::MobileRT::Triangle>>(this->triangles_, intersection, ray);
    intersection =
        trace<::MobileRT::Primitive<::MobileRT::Sphere>>(this->spheres_, intersection, ray);
    intersection =
        trace<::MobileRT::Primitive<::MobileRT::Plane>>(this->planes_, intersection, ray);
    intersection = traceLights(intersection, ray);
    return intersection;
}

template<typename T>
Intersection Scene::shadowTrace(::std::vector<T> &primitives, Intersection intersection,
                        const Ray &ray) const noexcept {
    for (T &primitive : primitives) {
        const float lastDist {intersection.length_};
        intersection = primitive.intersect(intersection, ray);
        if (intersection.length_ < lastDist) {
            return intersection;
        }
    }
    return intersection;
}

Intersection Scene::shadowTrace(Intersection intersection, const Ray &ray) noexcept {
    intersection =
            shadowTrace<::MobileRT::Primitive<Triangle>>(this->triangles_, intersection, ray);
    intersection =
            shadowTrace<::MobileRT::Primitive<Sphere>>(this->spheres_, intersection, ray);
    intersection =
            shadowTrace<::MobileRT::Primitive<Plane>>(this->planes_, intersection, ray);
    return intersection;
}

void Scene::resetSampling() noexcept {
    for (const auto &light : this->lights_) {
        light->resetSampling();
    }
}

void Scene::AABBbounds(const AABB &box, ::glm::vec3 *const min, ::glm::vec3 *const max) {
    *min = ::glm::min(box.pointMin_, *min);

    *max = ::glm::max(box.pointMax_, *max);
}
