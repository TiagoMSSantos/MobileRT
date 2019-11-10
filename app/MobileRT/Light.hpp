#ifndef MOBILERT_LIGHT_HPP
#define MOBILERT_LIGHT_HPP

#include "MobileRT/Intersection.hpp"
#include "MobileRT/Ray.hpp"
#include <glm/glm.hpp>

namespace MobileRT {
    class Light {
    public:
        const Material radiance_{};

    public:
        explicit Light () noexcept = delete;

        explicit Light(const Material &radiance) noexcept;

        Light(const Light &light) noexcept = delete;

        Light(Light &&light) noexcept = delete;

        virtual ~Light() noexcept;

        Light &operator=(const Light &light) noexcept = delete;

        Light &operator=(Light &&light) noexcept = delete;

        virtual ::glm::vec3 getPosition() noexcept = 0;

        virtual void resetSampling() noexcept = 0;

        virtual Intersection intersect(Intersection intersection, const Ray &ray) const noexcept = 0;
    };
}//namespace MobileRT

#endif //MOBILERT_LIGHT_HPP
