#ifndef MOBILERT_RENDERER_HPP
#define MOBILERT_RENDERER_HPP

#include "MobileRT/Camera.hpp"
#include "MobileRT/Sampler.hpp"
#include "MobileRT/Shader.hpp"
#include "MobileRT/Utils.hpp"
#include <cmath>
#include <thread>

namespace MobileRT {
    class Renderer final {
    public:
        ::std::unique_ptr<Camera> camera_ {};
        ::std::unique_ptr<Shader> shader_ {};

    private:
        ::std::unique_ptr<Sampler> samplerPixel_ {};
        ::std::int32_t blockSizeX_ {};
        ::std::int32_t blockSizeY_ {};
        ::std::int32_t sample_ {};
        const ::std::int32_t width_ {};
        const ::std::int32_t height_ {};
        const ::std::int32_t domainSize_ {};
        const ::std::int32_t resolution_ {};
        const ::std::int32_t samplesPixel_ {};
        ::std::atomic<::std::int32_t> block_ {};

    private:
        void renderScene(::std::int32_t *bitmap, ::std::int32_t tid) noexcept;
        float getTile(const ::std::int32_t sample) noexcept;

    public:
        explicit Renderer () noexcept = delete;

        explicit Renderer(::std::unique_ptr<Shader> shader,
                          ::std::unique_ptr<Camera> camera,
                          ::std::unique_ptr<Sampler> samplerPixel,
                          ::std::int32_t width, ::std::int32_t height,
                          ::std::int32_t samplesPixel) noexcept;

        Renderer(const Renderer &renderer) noexcept = delete;

        Renderer(Renderer &&renderer) noexcept = delete;

        ~Renderer() noexcept = default;

        Renderer &operator=(const Renderer &renderer) noexcept = delete;

        Renderer &operator=(Renderer &&renderer) noexcept = delete;

        void renderFrame(::std::int32_t *bitmap, ::std::int32_t numThreads) noexcept;

        void stopRender() noexcept;

        ::std::int32_t getSample() const noexcept;
    };
}//namespace MobileRT

#endif //MOBILERT_RENDERER_HPP
