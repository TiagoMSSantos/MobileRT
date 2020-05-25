#ifndef MOBILERT_RENDERER_HPP
#define MOBILERT_RENDERER_HPP

#include "MobileRT/Camera.hpp"
#include "MobileRT/Sampler.hpp"
#include "MobileRT/Shader.hpp"
#include "MobileRT/Utils.hpp"
#include <cmath>
#include <thread>

namespace MobileRT {
    /**
     * The main class of the Ray Tracer engine.
     * After setup this object, it provides methods to start and stop the rendering process of a scene.
     */
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
        ::std::int32_t samplesPixel_ {};
        ::std::atomic<::std::int32_t> block_ {};

    private:
        void renderScene(::std::int32_t *bitmap, ::std::int32_t tid);
        float getTile(::std::int32_t sample);

    public:
        explicit Renderer () = delete;

        explicit Renderer(::std::unique_ptr<Shader> shader,
                          ::std::unique_ptr<Camera> camera,
                          ::std::unique_ptr<Sampler> samplerPixel,
                          ::std::int32_t width, ::std::int32_t height,
                          ::std::int32_t samplesPixel);

        Renderer(const Renderer &renderer) = delete;

        Renderer(Renderer &&renderer) noexcept = delete;

        ~Renderer() = default;

        Renderer &operator=(const Renderer &renderer) = delete;

        Renderer &operator=(Renderer &&renderer) noexcept = delete;

        void renderFrame(::std::int32_t *bitmap, ::std::int32_t numThreads);

        void stopRender();

        ::std::int32_t getSample() const;
    };
}//namespace MobileRT

#endif //MOBILERT_RENDERER_HPP
