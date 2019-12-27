#include "MobileRT/Material.hpp"
#include "MobileRT/Utils.hpp"

using ::MobileRT::Material;

Material::Material(
    const ::glm::vec3 &kD, const ::glm::vec3 &kS,
    const ::glm::vec3 &kT, const float refractiveIndice,
    const ::glm::vec3 &lE) noexcept :
    Le_ {lE},
    Kd_ {kD},
    Ks_ {kS},
    Kt_ {kT},
    refractiveIndice_ {refractiveIndice} {
}

bool Material::operator==(const Material &material) noexcept {
    const auto sameKd {::MobileRT::equal(this->Kd_, material.Kd_)};
    const auto sameKs {::MobileRT::equal(this->Ks_, material.Ks_)};
    const auto sameKt {::MobileRT::equal(this->Kt_, material.Kt_)};
    const auto sameLe {::MobileRT::equal(this->Le_, material.Le_)};
    const auto sameRi {::MobileRT::equal(this->refractiveIndice_, material.refractiveIndice_)};
    const auto same {sameKd && sameKs && sameKt && sameLe && sameRi};
    return same;
}
