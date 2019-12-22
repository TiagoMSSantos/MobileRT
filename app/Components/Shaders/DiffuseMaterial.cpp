#include "Components/Shaders/DiffuseMaterial.hpp"

using ::Components::DiffuseMaterial;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Scene;

DiffuseMaterial::DiffuseMaterial(Scene scene, const Accelerator accelerator) noexcept :
        Shader{::std::move(scene), 0, accelerator} {
}

bool DiffuseMaterial::shade(
    ::glm::vec3 *const rgb, const Intersection &intersection, const Ray &/*ray*/) noexcept {

    const ::glm::vec3 &Le {intersection.material_->Le_};
    const ::glm::vec3 &kD {intersection.material_->Kd_};
    const ::glm::vec3 &kS {intersection.material_->Ks_};
    const ::glm::vec3 &kT {intersection.material_->Kt_};

    if (::glm::any(::glm::greaterThan(kD, ::glm::vec3 {0}))) {
        *rgb = kD;
    } else if (::glm::any(::glm::greaterThan(kS, ::glm::vec3 {0}))) {
        *rgb = kS;
    } else if (::glm::any(::glm::greaterThan(kT, ::glm::vec3 {0}))) {
        *rgb = kT;
    } else if (::glm::any(::glm::greaterThan(Le, ::glm::vec3 {0}))) {
        *rgb = Le;
    }
    return false;
}
