#include "MobileRT/Scene.hpp"

using ::MobileRT::AABB;
using ::MobileRT::Scene;
using ::MobileRT::Plane;
using ::MobileRT::Sphere;
using ::MobileRT::Triangle;
using ::MobileRT::Light;

Scene::~Scene() {
    //may not free the memory
    this->planes_.clear();
    this->spheres_.clear();
    this->triangles_.clear();
    this->lights_.clear();

    //force free memory
    ::std::vector<Plane> {}.swap(this->planes_);
    ::std::vector<Sphere> {}.swap(this->spheres_);
    ::std::vector<Triangle> {}.swap(this->triangles_);
    ::std::vector<::std::unique_ptr<Light>> {}.swap(this->lights_);

    LOG("SCENE DELETED");
}

AABB Scene::getBoxBounds(const AABB &box1, const AABB &box2) {
    const AABB box {::glm::min(box1.pointMin_, box2.pointMin_), ::glm::max(box1.pointMax_, box2.pointMax_)};
    return box;
}
