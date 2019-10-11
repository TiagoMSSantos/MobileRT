#ifndef COMPONENTS_LIGHTS_POINTLIGHT_HPP
#define COMPONENTS_LIGHTS_POINTLIGHT_HPP

#include "MobileRT/Light.hpp"

namespace Components {

    class PointLight final : public ::MobileRT::Light {
    private:
        ::glm::vec3 position_{};

    public:
        explicit PointLight () noexcept = delete;

        explicit PointLight(
            const ::MobileRT::Material &radiance, const ::glm::vec3 &position) noexcept;

        PointLight(const PointLight &pointLight) noexcept = delete;

        PointLight(PointLight &&pointLight) noexcept = delete;

        ~PointLight() noexcept final = default;

        PointLight &operator=(const PointLight &pointLight) noexcept = delete;

        PointLight &operator=(PointLight &&pointLight) noexcept = delete;

        ::glm::vec3 getPosition() noexcept final;

        void resetSampling() noexcept final;

        ::MobileRT::Intersection intersect(
            ::MobileRT::Intersection intersection,
            const ::MobileRT::Ray &ray) const noexcept final;
    };
}//namespace Components

#endif //COMPONENTS_LIGHTS_POINTLIGHT_HPP
