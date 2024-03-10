#include "MobileRT/Material.hpp"
#include "MobileRT/Utils/Utils.hpp"

using ::MobileRT::Material;
using ::MobileRT::Texture;

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
    const ::glm::vec3 &lE, Texture texture) noexcept :
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
bool Material::operator==(const Material &material) const {
    const bool sameKd {::MobileRT::equal(this->Kd_, material.Kd_)};
    const bool sameKs {::MobileRT::equal(this->Ks_, material.Ks_)};
    const bool sameKt {::MobileRT::equal(this->Kt_, material.Kt_)};
    const bool sameLe {::MobileRT::equal(this->Le_, material.Le_)};
    const bool sameRi {::MobileRT::equal(this->refractiveIndice_, material.refractiveIndice_)};
    const bool sameTexture {this->texture_ == material.texture_};
    const bool same {sameKd && sameKs && sameKt && sameLe && sameRi && sameTexture};
    return same;
}
