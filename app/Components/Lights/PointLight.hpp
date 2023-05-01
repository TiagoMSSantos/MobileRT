#ifndef COMPONENTS_LIGHTS_POINTLIGHT_HPP
#define COMPONENTS_LIGHTS_POINTLIGHT_HPP

#include "MobileRT/Light.hpp"

namespace Components {

    /**
     *  A light that gets emitted from a single point in all directions.
     */
    class PointLight final : public ::MobileRT::Light {
    private:
        ::glm::vec3 position_ {};

    public:
        explicit PointLight() = delete;

        explicit PointLight(const ::MobileRT::Material &radiance, const ::glm::vec3 &position);

        PointLight(const PointLight &pointLight) = delete;

        PointLight(PointLight &&pointLight) noexcept = delete;

        ~PointLight() final = default;

        PointLight &operator=(const PointLight &pointLight) = delete;

        PointLight &operator=(PointLight &&pointLight) noexcept = delete;

        ::glm::vec3 getPosition() final;

        void resetSampling() final;

        ::MobileRT::Intersection intersect(::MobileRT::Intersection &&intersection) final;
    };
}//namespace Components

#endif //COMPONENTS_LIGHTS_POINTLIGHT_HPP
