#ifndef MOBILERAYTRACER_DIFFUSEMATERIAL_HPP
#define MOBILERAYTRACER_DIFFUSEMATERIAL_HPP

#include "MobileRT/Shader.hpp"

namespace Components {

    class DiffuseMaterial final : public ::MobileRT::Shader {
    private:
        bool shade(
            ::glm::vec3 *rgb,
            const ::MobileRT::Intersection &intersection,
            const ::MobileRT::Ray &ray) noexcept final;

    public:
        explicit DiffuseMaterial () noexcept = delete;

        explicit DiffuseMaterial(::MobileRT::Scene scene, Accelerator accelerator) noexcept;

        DiffuseMaterial(const DiffuseMaterial &diffuseMaterial) noexcept = delete;

        DiffuseMaterial(DiffuseMaterial &&diffuseMaterial) noexcept = delete;

        ~DiffuseMaterial() noexcept final = default;

        DiffuseMaterial &operator=(const DiffuseMaterial &diffuseMaterial) noexcept = delete;

        DiffuseMaterial &operator=(DiffuseMaterial &&diffuseMaterial) noexcept = delete;
    };
}//namespace Components

#endif //MOBILERAYTRACER_DIFFUSEMATERIAL_HPP
