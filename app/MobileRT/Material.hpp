#ifndef MOBILERT_MATERIAL_HPP
#define MOBILERT_MATERIAL_HPP

#include "Texture.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    /**
     * A class which represents the material of a primitive in the scene.
     * <br>
     * A material consists of the type of interactions that a light does when it hits the object.
     */
    class Material final {
    public:
        /**
         * The emission light.
         */
        ::glm::vec3 Le_ {};

        /**
         * The diffuse reflection.
         */
        ::glm::vec3 Kd_ {};

        /**
         * The specular reflection.
         */
        ::glm::vec3 Ks_ {};

        /**
         * The specular transmission.
         */
        ::glm::vec3 Kt_ {};

        /**
         * The refractive indice.
         */
        float refractiveIndice_ {};

        /**
         * The texture of the material.
         */
        Texture texture_ {};

    public:
        explicit Material() = default;

        explicit Material(
            const ::glm::vec3 &kD,
            const ::glm::vec3 &kS = ::glm::vec3 {},
            const ::glm::vec3 &kT = ::glm::vec3 {},
            float refractiveIndice = 1.0F,
            const ::glm::vec3 &lE = ::glm::vec3 {},
            Texture texture = Texture {}) noexcept;

        Material(const Material &material) = default;

        Material(Material &&material) noexcept = default;

        ~Material() = default;

        Material &operator=(const Material &material) = default;

        Material &operator=(Material &&material) noexcept = default;

        bool operator==(const Material &material) const;
    };
}//namespace MobileRT

#endif //MOBILERT_MATERIAL_HPP
