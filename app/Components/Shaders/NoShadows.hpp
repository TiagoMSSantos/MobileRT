#ifndef COMPONENTS_SHADERS_NOSHADOWS_HPP
#define COMPONENTS_SHADERS_NOSHADOWS_HPP

#include "MobileRT/Shader.hpp"

namespace Components {

    class NoShadows final : public ::MobileRT::Shader {
    private:
        bool shade(
            ::glm::vec3 *rgb,
            const ::MobileRT::Intersection &intersection,
            const ::MobileRT::Ray &ray) noexcept final;

    public:
        explicit NoShadows () noexcept = delete;

        explicit NoShadows(
            ::MobileRT::Scene scene,
            ::std::uint32_t samplesLight,
            Accelerator accelerator) noexcept;

        NoShadows(const NoShadows &noShadows) noexcept = delete;

        NoShadows(NoShadows &&noShadows) noexcept = delete;

        ~NoShadows() noexcept final = default;

        NoShadows &operator=(const NoShadows &noShadows) noexcept = delete;

        NoShadows &operator=(NoShadows &&noShadows) noexcept = delete;
    };
}//namespace Components

#endif //COMPONENTS_SHADERS_NOSHADOWS_HPP
