#ifndef COMPONENTS_LIGHTS_AREALIGHT_HPP
#define COMPONENTS_LIGHTS_AREALIGHT_HPP

#include "MobileRT/Light.hpp"
#include "MobileRT/Sampler.hpp"
#include "MobileRT/Shapes/Triangle.hpp"
#include "MobileRT/Utils/Utils.hpp"
#include <memory>

namespace Components {

    /**
     *  An area light is a light that casts directional light rays from within a set boundary,
     *  in this case, in the format of a triangle.
     *  All other light types emit light from a single point, where an area light emits light from
     *  an entire area which is generally more realistic.
     */
    class AreaLight final : public ::MobileRT::Light {
    private:
        ::MobileRT::Triangle triangle_;
        ::std::unique_ptr<::MobileRT::Sampler> samplerPointLight_ {};

    public:
        explicit AreaLight() = delete;

        explicit AreaLight(
            ::MobileRT::Material radiance,
            ::std::unique_ptr<::MobileRT::Sampler> samplerPointLight,
            ::MobileRT::Triangle triangle);

        AreaLight(const AreaLight &areaLight) = delete;

        AreaLight(AreaLight &&areaLight) noexcept = delete;

        ~AreaLight() final = default;

        AreaLight &operator=(const AreaLight &areaLight) = delete;

        AreaLight &operator=(AreaLight &&areaLight) noexcept = delete;

        ::glm::vec3 getPosition() final;

        void resetSampling() final;

        ::MobileRT::Intersection intersect(::MobileRT::Intersection &&intersection) final;
    };
}//namespace Components

#endif //COMPONENTS_LIGHTS_AREALIGHT_HPP
