#include "MobileRT/Material.hpp"
#include "MobileRT/Utils.hpp"

using ::MobileRT::Material;

Material::Material(
    const ::glm::vec3 &kD, const ::glm::vec3 &kS,
    const ::glm::vec3 &kT, const float refractiveIndice,
    const ::glm::vec3 &lE, const Texture &texture) :
    Le_ {lE},
    Kd_ {kD},
    Ks_ {kS},
    Kt_ {kT},
    refractiveIndice_ {refractiveIndice},
    texture_ {texture} {
}

bool Material::operator==(const Material &material) {
    const auto sameKd {::MobileRT::equal(this->Kd_, material.Kd_)};
    const auto sameKs {::MobileRT::equal(this->Ks_, material.Ks_)};
    const auto sameKt {::MobileRT::equal(this->Kt_, material.Kt_)};
    const auto sameLe {::MobileRT::equal(this->Le_, material.Le_)};
    const auto sameRi {::MobileRT::equal(this->refractiveIndice_, material.refractiveIndice_)};
    const auto sameTexture {this->texture_ == material.texture_};
    const auto same {sameKd && sameKs && sameKt && sameLe && sameRi && sameTexture};
    return same;
}
