#include "Components/Shaders/DiffuseMaterial.hpp"

using ::Components::DiffuseMaterial;
using ::MobileRT::Intersection;
using ::MobileRT::Scene;

DiffuseMaterial::DiffuseMaterial(Scene scene, const Accelerator accelerator) :
    Shader {::std::move(scene), 0, accelerator} {
}

bool DiffuseMaterial::shade(::glm::vec3 *const rgb, const Intersection &intersection) {
    const ::glm::vec3 &lE {intersection.material_->Le_};
    const ::glm::vec3 &kD {intersection.material_->Kd_};
    const ::glm::vec3 &kS {intersection.material_->Ks_};
    const ::glm::vec3 &kT {intersection.material_->Kt_};

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
