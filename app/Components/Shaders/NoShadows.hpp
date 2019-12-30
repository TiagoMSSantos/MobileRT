#ifndef COMPONENTS_SHADERS_NOSHADOWS_HPP
#define COMPONENTS_SHADERS_NOSHADOWS_HPP

#include "MobileRT/Shader.hpp"

namespace Components {

    class NoShadows final : public ::MobileRT::Shader {
    private:
        bool shade(
            ::glm::vec3 *rgb,
            const ::MobileRT::Intersection &intersection,
            const ::MobileRT::Ray &ray) final;

    public:
        explicit NoShadows() = delete;

        explicit NoShadows(
            ::MobileRT::Scene scene,
            ::std::int32_t samplesLight,
            ::MobileRT::Shader::Accelerator accelerator);

        NoShadows(const NoShadows &noShadows) = delete;

        NoShadows(NoShadows &&noShadows) noexcept = delete;

        ~NoShadows() final = default;

        NoShadows &operator=(const NoShadows &noShadows) = delete;

        NoShadows &operator=(NoShadows &&noShadows) noexcept = delete;
    };
}//namespace Components

#endif //COMPONENTS_SHADERS_NOSHADOWS_HPP
