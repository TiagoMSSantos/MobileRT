#include "MobileRT/Scene.hpp"

using ::MobileRT::Scene;
using ::MobileRT::Plane;
using ::MobileRT::Sphere;
using ::MobileRT::Triangle;
using ::MobileRT::Primitive;
using ::MobileRT::Light;
using ::MobileRT::Intersection;

Scene::~Scene() noexcept {
    //may not free the memory
    this->planes_.clear();
    this->spheres_.clear();
    this->triangles_.clear();
    this->lights_.clear();

    //force free memory
    ::std::vector<Primitive<Plane>> {}.swap(this->planes_);
    ::std::vector<Primitive<Sphere>> {}.swap(this->spheres_);
    ::std::vector<Primitive<Triangle>> {}.swap(this->triangles_);
    ::std::vector<::std::unique_ptr<Light>> {}.swap(this->lights_);

    LOG("SCENE DELETED");
}

void Scene::getAABBbounds(const AABB &box, ::glm::vec3 *const min, ::glm::vec3 *const max) {
    *min = ::glm::min(box.pointMin_, *min);
    *max = ::glm::max(box.pointMax_, *max);
}
