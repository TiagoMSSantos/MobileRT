#ifndef MOBILERT_MATERIAL_HPP
#define MOBILERT_MATERIAL_HPP

#include "Texture.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    class Material final {
    public:
        ::glm::vec3 Le_ {};   // emission light
        ::glm::vec3 Kd_ {};   // diffuse reflection
        ::glm::vec3 Ks_ {};   // specular reflection
        ::glm::vec3 Kt_ {};   // specular transmission
        float refractiveIndice_ {};
        Texture texture_ {};

    public:
        explicit Material() = default;

        explicit Material(
            const ::glm::vec3 &kD,
            const ::glm::vec3 &kS = ::glm::vec3 {},
            const ::glm::vec3 &kT = ::glm::vec3 {},
            float refractiveIndice = 1.0F,
            const ::glm::vec3 &lE = ::glm::vec3 {},
            Texture texture = Texture {});

        Material(const Material &material) = default;

        Material(Material &&material) noexcept = default;

        ~Material() = default;

        Material &operator=(const Material &material) = default;

        Material &operator=(Material &&material) noexcept = default;

        bool operator==(const Material &material);
    };
}//namespace MobileRT

#endif //MOBILERT_MATERIAL_HPP
