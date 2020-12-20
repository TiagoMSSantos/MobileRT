#ifndef MOBILERT_LIGHT_HPP
#define MOBILERT_LIGHT_HPP

#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    /**
     * A class which represents a light in the scene.
     * <br>
     * A light is a primitive which contains a material that emits light (i.e. a source of light).
     */
    class Light {
    public:
        Material radiance_ {};

    public:
        explicit Light() = delete;

        explicit Light(Material radiance);

        Light(const Light &light) = delete;

        Light(Light &&light) noexcept = delete;

        virtual ~Light();

        Light &operator=(const Light &light) = delete;

        Light &operator=(Light &&light) noexcept = delete;

        /**
         * Gets the position of the light.
         *
         * @return The position of the light.
         */
        virtual ::glm::vec3 getPosition() = 0;

        /**
         * Resets the sampling counter.
         */
        virtual void resetSampling() = 0;

        /**
         * Determines if a ray intersects this light or not and calculates the intersection point.
         *
         * @param intersection The previous intersection of the ray in the scene.
         * @return The intersection point.
         */
        virtual Intersection intersect(Intersection intersection) = 0;
    };
}//namespace MobileRT

#endif //MOBILERT_LIGHT_HPP
