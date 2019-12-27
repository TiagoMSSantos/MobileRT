#ifndef COMPONENTS_SHADERS_PATHTRACER_HPP
#define COMPONENTS_SHADERS_PATHTRACER_HPP

#include "MobileRT/Sampler.hpp"
#include "MobileRT/Shader.hpp"
#include <memory>
#include <random>

namespace Components {

    class PathTracer final : public ::MobileRT::Shader {
    private:
        ::std::unique_ptr<::MobileRT::Sampler> samplerRussianRoulette_{};

    private:
        bool shade(
            ::glm::vec3 *rgb,
            const ::MobileRT::Intersection &intersection,
            const ::MobileRT::Ray &ray) noexcept final;

    public:
        explicit PathTracer() noexcept = delete;

        explicit PathTracer(::MobileRT::Scene scene,
                            ::std::unique_ptr<::MobileRT::Sampler> samplerRussianRoulette,
                            ::std::int32_t samplesLight, Accelerator accelerator) noexcept;

        PathTracer(const PathTracer &pathTracer) noexcept = delete;

        PathTracer(PathTracer &&pathTracer) noexcept = delete;

        ~PathTracer() noexcept final = default;

        PathTracer &operator=(const PathTracer &pathTracer) noexcept = delete;

        PathTracer &operator=(PathTracer &&pathTracer) noexcept = delete;

        void resetSampling() noexcept final;
    };
}//namespace Components

#endif //COMPONENTS_SHADERS_PATHTRACER_HPP
