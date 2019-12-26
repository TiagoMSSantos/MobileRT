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
        ::std::uint32_t blockSizeX_ {};
        ::std::uint32_t blockSizeY_ {};
        ::std::uint32_t sample_ {};
        const ::std::uint32_t width_ {};
        const ::std::uint32_t height_ {};
        const ::std::uint32_t domainSize_ {};
        const ::std::uint32_t resolution_ {};
        const ::std::uint32_t samplesPixel_ {};
        ::std::atomic<::std::uint32_t> block_ {};

    private:
        void renderScene(::std::uint32_t *bitmap, ::std::int32_t tid, ::std::uint32_t width) noexcept;
        float getTile(const uint32_t sample) noexcept;

    public:
        explicit Renderer () noexcept = delete;

        explicit Renderer(::std::unique_ptr<Shader> shader,
                          ::std::unique_ptr<Camera> camera,
                          ::std::unique_ptr<Sampler> samplerPixel,
                          ::std::uint32_t width, ::std::uint32_t height,
                          ::std::uint32_t samplesPixel) noexcept;

        Renderer(const Renderer &renderer) noexcept = delete;

        Renderer(Renderer &&renderer) noexcept = delete;

        ~Renderer() noexcept = default;

        Renderer &operator=(const Renderer &renderer) noexcept = delete;

        Renderer &operator=(Renderer &&renderer) noexcept = delete;

        void renderFrame(::std::uint32_t *bitmap, ::std::int32_t numThreads, ::std::uint32_t stride) noexcept;

        void stopRender() noexcept;

        ::std::uint32_t getSample() const noexcept;
    };
}//namespace MobileRT

#endif //MOBILERT_RENDERER_HPP
