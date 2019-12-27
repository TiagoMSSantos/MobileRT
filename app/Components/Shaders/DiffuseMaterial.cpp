#include "Components/Shaders/DiffuseMaterial.hpp"

using ::Components::DiffuseMaterial;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Scene;

DiffuseMaterial::DiffuseMaterial(Scene scene, const Accelerator accelerator) noexcept :
    Shader {::std::move(scene), 0, accelerator} {
}

bool DiffuseMaterial::shade(::glm::vec3 *const rgb, const Intersection &intersection, const Ray &/*ray*/) noexcept {
    const auto &lE {intersection.material_->Le_};
    const auto &kD {intersection.material_->Kd_};
    const auto &kS {intersection.material_->Ks_};
    const auto &kT {intersection.material_->Kt_};

    if (::glm::any(::glm::greaterThan(kD, ::glm::vec3 {0}))) {
        *rgb = kD;
    } else if (::glm::any(::glm::greaterThan(kS, ::glm::vec3 {0}))) {
        *rgb = kS;
    } else if (::glm::any(::glm::greaterThan(kT, ::glm::vec3 {0}))) {
        *rgb = kT;
    } else if (::glm::any(::glm::greaterThan(lE, ::glm::vec3 {0}))) {
        *rgb = lE;
    }
    return false;
}
