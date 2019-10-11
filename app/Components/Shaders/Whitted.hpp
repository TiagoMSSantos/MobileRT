//
// Created by Tiago on 16-10-2016.
//

#ifndef COMPONENTS_SHADERS_WHITTED_HPP
#define COMPONENTS_SHADERS_WHITTED_HPP

#include "MobileRT/Shader.hpp"

namespace Components {

    class Whitted final : public ::MobileRT::Shader {
    private:
        bool shade(
            ::glm::vec3 *rgb,
            const ::MobileRT::Intersection &intersection,
            const ::MobileRT::Ray &ray) noexcept final;

    public:
        explicit Whitted () noexcept = delete;

        explicit Whitted(
            ::MobileRT::Scene scene,
            ::std::uint32_t samplesLight,
            Accelerator accelerator) noexcept;

        Whitted(const Whitted &whitted) noexcept = delete;

        Whitted(Whitted &&whitted) noexcept = delete;

        ~Whitted() noexcept final = default;

        Whitted &operator=(const Whitted &whitted) noexcept = delete;

        Whitted &operator=(Whitted &&whitted) noexcept = delete;
    };
}//namespace Components

#endif //COMPONENTS_SHADERS_WHITTED_HPP
