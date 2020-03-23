#include "Components/Shaders/DiffuseMaterial.hpp"

using ::Components::DiffuseMaterial;
using ::MobileRT::Intersection;
using ::MobileRT::Ray;
using ::MobileRT::Scene;

DiffuseMaterial::DiffuseMaterial(Scene scene, const Accelerator accelerator) :
    Shader {::std::move(scene), 0, accelerator} {
}

bool DiffuseMaterial::shade(::glm::vec3 *const rgb, const Intersection &intersection, const Ray &/*ray*/) {
    const auto &lE {intersection.material_->Le_};
    const auto &kD {intersection.material_->Kd_};
    const auto &kS {intersection.material_->Ks_};
    const auto &kT {intersection.material_->Kt_};

    if (::MobileRT::hasPositiveValue(kD)) {
        *rgb = kD;
    } else if (::MobileRT::hasPositiveValue(kS)) {
        *rgb = kS;
    } else if (::MobileRT::hasPositiveValue(kT)) {
        *rgb = kT;
    } else if (::MobileRT::hasPositiveValue(lE)) {
        *rgb = lE;
    }
    return false;
}
