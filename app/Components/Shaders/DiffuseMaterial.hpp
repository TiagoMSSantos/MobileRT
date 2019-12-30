#ifndef MOBILERAYTRACER_DIFFUSEMATERIAL_HPP
#define MOBILERAYTRACER_DIFFUSEMATERIAL_HPP

#include "MobileRT/Shader.hpp"

namespace Components {

    class DiffuseMaterial final : public ::MobileRT::Shader {
    private:
        bool shade(
            ::glm::vec3 *rgb,
            const ::MobileRT::Intersection &intersection,
            const ::MobileRT::Ray &ray) final;

    public:
        explicit DiffuseMaterial () = delete;

        explicit DiffuseMaterial(::MobileRT::Scene scene, ::MobileRT::Shader::Accelerator accelerator);

        DiffuseMaterial(const DiffuseMaterial &diffuseMaterial) = delete;

        DiffuseMaterial(DiffuseMaterial &&diffuseMaterial) noexcept = delete;

        ~DiffuseMaterial() final = default;

        DiffuseMaterial &operator=(const DiffuseMaterial &diffuseMaterial) = delete;

        DiffuseMaterial &operator=(DiffuseMaterial &&diffuseMaterial) noexcept = delete;
    };
}//namespace Components

#endif //MOBILERAYTRACER_DIFFUSEMATERIAL_HPP
