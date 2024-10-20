#ifndef COMPONENTS_SHADERS_WHITTED_HPP
#define COMPONENTS_SHADERS_WHITTED_HPP

#include "MobileRT/Shader.hpp"

namespace Components {

    class Whitted final : public ::MobileRT::Shader {
    private:
        bool shade(
            ::glm::vec3 *rgb,
            const ::MobileRT::Intersection &intersection) final;

    public:
        explicit Whitted () = delete;

        explicit Whitted(
            ::MobileRT::Scene scene,
            ::std::int32_t samplesLight,
            ::MobileRT::Shader::Accelerator accelerator);

        Whitted(const Whitted &whitted) = delete;

        Whitted(Whitted &&whitted) noexcept = delete;

        ~Whitted() final = default;

        Whitted &operator=(const Whitted &whitted) = delete;

        Whitted &operator=(Whitted &&whitted) noexcept = delete;
    };
}//namespace Components

#endif //COMPONENTS_SHADERS_WHITTED_HPP
