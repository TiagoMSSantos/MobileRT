#include "MobileRT/Material.hpp"
#include "MobileRT/Utils.hpp"

using ::MobileRT::Material;

/**
 * The constructor.
 *
 * @param kD               The diffuse reflection.
 * @param kS               The specular reflection.
 * @param kT               The specular refraction.
 * @param refractiveIndice The refractive index or index of refraction of the material.
 * @param lE               The emission of light, in case of a light source.
 * @param texture          The texture of the material.
 */
Material::Material(
    const ::glm::vec3 &kD, const ::glm::vec3 &kS,
    const ::glm::vec3 &kT, const float refractiveIndice,
    const ::glm::vec3 &lE, Texture texture) :
    Le_ {lE},
    Kd_ {kD},
    Ks_ {kS},
    Kt_ {kT},
    refractiveIndice_ {refractiveIndice},
    texture_ {::std::move(texture)} {
}

/**
 * The operator equals.
 * This method determine if a material is the same as this one.
 *
 * @param material A material.
 * @return Whether the material is equal to this one.
 */
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
