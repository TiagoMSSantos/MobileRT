#include "MobileRT/Material.hpp"

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
