#ifndef COMPONENTS_LIGHTS_AREALIGHT_HPP
#define COMPONENTS_LIGHTS_AREALIGHT_HPP

#include "MobileRT/Light.hpp"
#include "MobileRT/Sampler.hpp"
#include "MobileRT/Shapes/Triangle.hpp"
#include "MobileRT/Utils.hpp"
#include <memory>

namespace Components {

    class AreaLight final : public ::MobileRT::Light {
    private:
        ::MobileRT::Triangle triangle_;
        ::std::unique_ptr<::MobileRT::Sampler> samplerPointLight_ {};

    public:
        explicit AreaLight() noexcept = delete;

        explicit AreaLight(
            const ::MobileRT::Material &radiance,
            ::std::unique_ptr<::MobileRT::Sampler> samplerPointLight,
            const ::glm::vec3 &pointA,
            const ::glm::vec3 &pointB,
            const ::glm::vec3 &pointC) noexcept;

        AreaLight(const AreaLight &areaLight) noexcept = delete;

        AreaLight(AreaLight &&areaLight) noexcept = delete;

        ~AreaLight() noexcept final = default;

        AreaLight &operator=(const AreaLight &areaLight) noexcept = delete;

        AreaLight &operator=(AreaLight &&areaLight) noexcept = delete;

        ::glm::vec3 getPosition() noexcept final;

        void resetSampling() noexcept final;

        ::MobileRT::Intersection intersect(
            ::MobileRT::Intersection intersection,
            const ::MobileRT::Ray &ray) noexcept final;
    };
}//namespace Components

#endif //COMPONENTS_LIGHTS_AREALIGHT_HPP
