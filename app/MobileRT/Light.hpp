#ifndef MOBILERT_LIGHT_HPP
#define MOBILERT_LIGHT_HPP

#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
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

        virtual ::glm::vec3 getPosition() = 0;

        virtual void resetSampling() = 0;

        virtual Intersection intersect(Intersection intersection, const Ray &ray) = 0;
    };
}//namespace MobileRT

#endif //MOBILERT_LIGHT_HPP
