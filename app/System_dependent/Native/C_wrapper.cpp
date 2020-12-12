#include "C_wrapper.h"
#include "Components/Cameras/Orthographic.hpp"
#include "Components/Cameras/Perspective.hpp"
#include "Components/Lights/AreaLight.hpp"
#include "Components/Lights/PointLight.hpp"
#include "Components/Loaders/CameraFactory.hpp"
#include "Components/Loaders/OBJLoader.hpp"
#include "Components/Samplers/Constant.hpp"
#include "Components/Samplers/HaltonSeq.hpp"
#include "Components/Samplers/MersenneTwister.hpp"
#include "Components/Samplers/StaticHaltonSeq.hpp"
#include "Components/Samplers/StaticMersenneTwister.hpp"
#include "Components/Samplers/Stratified.hpp"
#include "Components/Shaders/DepthMap.hpp"
#include "Components/Shaders/DiffuseMaterial.hpp"
#include "Components/Shaders/NoShadows.hpp"
#include "Components/Shaders/PathTracer.hpp"
#include "Components/Shaders/Whitted.hpp"
#include "MobileRT/Renderer.hpp"
#include "MobileRT/Scene.hpp"
#include "Scenes/Scenes.hpp"

#include <chrono>
#include <cstring>
#include <fstream>

static ::std::unique_ptr<::MobileRT::Renderer> renderer_ {};

/**
 * Helper method that starts the Ray Tracer engine.
 *
 * @param bitmap       The bitmap to where the rendered image should be put.
 * @param width        The width of the image to render.
 * @param height       The height of the image to render.
 * @param threads      The number of threads to be used by the Ray Tracer engine.
 * @param shader       The shader to be used.
 * @param sceneIndex   The scene index to render.
 * @param samplesPixel The number of samples per pixel to use.
 * @param samplesLight The number of samples per light to use.
 * @param repeats      The number of times to render the scene.
 * @param accelerator  The acceleration structure to use.
 * @param printStdOut  Whether or not the logs should be redirected to the standard output.
 * @param objFilePath  The path to the OBJ file of the scene.
 * @param mtlFilePath  The path to the MTL file of the scene.
 * @param camFilePath  The path to the CAM file of the scene.
 */
static void
work_thread(
    ::std::int32_t *const bitmap, const ::std::int32_t width,
    const ::std::int32_t height, const ::std::int32_t threads,
    const ::std::int32_t shader, const ::std::int32_t sceneIndex, const ::std::int32_t samplesPixel, const ::std::int32_t samplesLight,
    ::std::int32_t repeats, const ::std::int32_t accelerator, const bool printStdOut,
    const char *const objFilePath, const char *const mtlFilePath, const char *const camFilePath) {
    try {
        ::std::ostringstream ss {""};
        ::std::streambuf *old_buf_stdout {};
        ::std::streambuf *old_buf_stderr {};
        ::std::chrono::duration<double> timeCreating {};
        ::std::chrono::duration<double> timeRendering {};
        ::std::chrono::duration<double> timeLoading {};
        ::std::chrono::duration<double> timeFilling {};
        if (!printStdOut) {
            // Turn off redirection of logs to standard output
            old_buf_stdout = ::std::cout.rdbuf(ss.rdbuf());
            old_buf_stderr = ::std::cerr.rdbuf(ss.rdbuf());
        }
        {
            // Print debug information
            LOG_DEBUG("width_ = ", width);
            LOG_DEBUG("height_ = ", height);
            LOG_DEBUG("threads = ", threads);
            LOG_DEBUG("shader = ", shader);
            LOG_DEBUG("scene = ", sceneIndex);
            LOG_DEBUG("samplesPixel = ", samplesPixel);
            LOG_DEBUG("samplesLight = ", samplesLight);
            LOG_DEBUG("repeats = ", repeats);
            LOG_DEBUG("accelerator = ", accelerator);
            LOG_DEBUG("printStdOut = ", printStdOut);
            LOG_DEBUG("objFilePath = ", objFilePath);
            LOG_DEBUG("mtlFilePath = ", mtlFilePath);
            LOG_DEBUG("camFilePath = ", camFilePath);

            const auto ratio {static_cast<float> (width) / height};
            ::MobileRT::Scene scene {};
            ::std::unique_ptr<::MobileRT::Sampler> samplerPixel {};
            ::std::unique_ptr<::MobileRT::Shader> shader_ {};
            ::std::unique_ptr<::MobileRT::Camera> camera {};
            ::glm::vec3 maxDist {};

            // Setup scene and camera
            switch (sceneIndex) {
                case 0:
                    scene = cornellBox_Scene(::std::move(scene));
                    camera = cornellBox_Cam(ratio);
                    maxDist = ::glm::vec3{1, 1, 1};
                    break;

                case 1:
                    scene = spheres_Scene(::std::move(scene));
                    camera = spheres_Cam(ratio);
                    maxDist = ::glm::vec3{8, 8, 8};
                    break;

                case 2:
                    scene = cornellBox2_Scene(::std::move(scene));
                    camera = cornellBox2_Cam(ratio);
                    maxDist = ::glm::vec3{1, 1, 1};
                    break;

                case 3:
                    scene = spheres2_Scene(::std::move(scene));
                    camera = spheres2_Cam(ratio);
                    maxDist = ::glm::vec3 {8, 8, 8};
                    break;

                default: {
                    const auto startLoading {::std::chrono::system_clock::now()};
                    ::Components::OBJLoader objLoader {objFilePath, mtlFilePath};
                    if (!objLoader.isProcessed()) {
                        exit(0);
                    }
                    const auto endLoading {::std::chrono::system_clock::now()};
                    timeLoading = endLoading - startLoading;
                    LOG_DEBUG("OBJLoader loaded = ", timeLoading.count());
                    const auto startFilling {::std::chrono::system_clock::now()};
                    // "objLoader.fillScene(&scene, []() {return ::MobileRT::std::make_unique<::Components::HaltonSeq> ();});"
                    // "objLoader.fillScene(&scene, []() {return ::MobileRT::std::make_unique<::Components::MersenneTwister> ();});"
                    objLoader.fillScene(&scene, []() {return ::MobileRT::std::make_unique<Components::StaticHaltonSeq> (); });
                    // "objLoader.fillScene(&scene, []() {return ::MobileRT::std::make_unique<Components::StaticMersenneTwister> ();});"
                    const auto endFilling {::std::chrono::system_clock::now()};
                    timeFilling = endFilling - startFilling;
                    LOG_DEBUG("Scene filled = ", timeFilling.count());

                    const auto cameraFactory {::Components::CameraFactory()};
                    camera = cameraFactory.loadFromFile(camFilePath, ratio);
                    maxDist = ::glm::vec3 {1, 1, 1};
                }
                    break;
            }
            // Setup sampler
            if (samplesPixel > 1) {
                samplerPixel = ::MobileRT::std::make_unique<::Components::StaticHaltonSeq> ();
            } else {
                samplerPixel = ::MobileRT::std::make_unique<::Components::Constant> (0.5F);
            }
            // Start timer to measure latency of creating shader (including the build of
            // acceleration structure)
            const auto startCreating {::std::chrono::system_clock::now()};
            // Setup shader
            switch (shader) {
                case 1: {
                    shader_ = ::MobileRT::std::make_unique<::Components::Whitted> (
                    ::std::move(scene), samplesLight, ::MobileRT::Shader::Accelerator(accelerator)
                    );
                    break;
                }

                case 2: {
                    ::std::unique_ptr<MobileRT::Sampler> samplerRussianRoulette {
                            ::MobileRT::std::make_unique<::Components::StaticHaltonSeq> ()
                    };

                    shader_ = ::MobileRT::std::make_unique<::Components::PathTracer> (
                    ::std::move(scene), ::std::move(samplerRussianRoulette), samplesLight,
                    ::MobileRT::Shader::Accelerator(accelerator)
                    );
                    break;
                }

                case 3: {
                shader_ = ::MobileRT::std::make_unique<::Components::DepthMap> (
                    ::std::move(scene), maxDist, ::MobileRT::Shader::Accelerator(accelerator)
                );
                    break;
                }

                case 4: {
                shader_ = ::MobileRT::std::make_unique<::Components::DiffuseMaterial> (
                    ::std::move(scene), ::MobileRT::Shader::Accelerator(accelerator)
                );
                break;
                }

                default: {
                shader_ = ::MobileRT::std::make_unique<::Components::NoShadows> (
                    ::std::move(scene), samplesLight, ::MobileRT::Shader::Accelerator(accelerator)
                );
                break;
                }
            }
            // Stop timer
            const auto endCreating {::std::chrono::system_clock::now()};
            timeCreating = endCreating - startCreating;
            LOG_DEBUG("Shader created = ", timeCreating.count());

            const auto planes {static_cast<::std::int32_t> (shader_->getPlanes().size())};
            const auto spheres {static_cast<::std::int32_t> (shader_->getSpheres().size())};
            const auto triangles {static_cast<::std::int32_t> (shader_->getTriangles().size())};
            const auto numLights {static_cast<::std::int32_t> (shader_->getLights().size())};
            const auto nPrimitives {triangles + spheres + planes};

            LOG_INFO("Started creating Renderer");
            renderer_ = ::MobileRT::std::make_unique<::MobileRT::Renderer> (
                    ::std::move(shader_), ::std::move(camera), ::std::move(samplerPixel),
                    width, height, samplesPixel
            );

            // Print debug information
            LOG_DEBUG("TRIANGLES = ", triangles);
            LOG_DEBUG("SPHERES = ", spheres);
            LOG_DEBUG("PLANES = ", planes);
            LOG_DEBUG("PRIMITIVES = ", nPrimitives);
            LOG_DEBUG("LIGHTS = ", numLights);
            LOG_DEBUG("threads = ", threads);
            LOG_DEBUG("shader = ", shader);
            LOG_DEBUG("scene = ", sceneIndex);
            LOG_DEBUG("samplesPixel = ", samplesPixel);
            LOG_DEBUG("samplesLight = ", samplesLight);
            LOG_DEBUG("width_ = ", width);
            LOG_DEBUG("height_ = ", height);

            LOG_INFO("Started rendering scene");
            const auto startRendering {::std::chrono::system_clock::now()};
            do {
                // Render a frame
                renderer_->renderFrame(bitmap, threads);
                repeats--;
            } while (repeats > 0);
            const auto endRendering {::std::chrono::system_clock::now()};

            timeRendering = endRendering - startRendering;
            LOG_INFO("Finished rendering scene");
        }
        if (!printStdOut) {
            // Turn on redirection of logs to standard output
            ::std::cout.rdbuf(old_buf_stdout);
            ::std::cerr.rdbuf(old_buf_stderr);
        }

        // Print some latencies
        const auto renderingTime {timeRendering.count()};
        const auto castedRays {renderer_->getTotalCastedRays()};
        LOG_DEBUG("Loading Time in secs = ", timeLoading.count());
        LOG_DEBUG("Filling Time in secs = ", timeFilling.count());
        LOG_DEBUG("Creating Time in secs = ", timeCreating.count());
        LOG_DEBUG("Rendering Time in secs = ", renderingTime);
        LOG_DEBUG("Casted rays = ", castedRays);
        LOG_DEBUG("width_ = ", width);
        LOG_DEBUG("height_ = ", height);

        LOG_INFO("Total Millions rays per second = ", (static_cast<double> (castedRays) / renderingTime) / 1000000L);
    } catch (const ::std::bad_alloc &badAlloc) {
        LOG_ERROR("badAlloc: ", badAlloc.what());
    } catch (const ::std::exception &exception) {
        LOG_ERROR("exception: ", exception.what());
    } catch (...) {
        LOG_ERROR("Unknown error");
    }
}

/**
 * Helper method that stops the Ray Tracing process.
 */
void stopRender() {
    if (renderer_ != nullptr) {
        renderer_->stopRender();
    }
}

/**
 * Helper method that starts the Ray Tracer engine.
 *
 * @param bitmap       The bitmap to where the rendered image should be put.
 * @param width        The width of the image to render.
 * @param height       The height of the image to render.
 * @param threads      The number of threads to be used by the Ray Tracer engine.
 * @param shader       The shader to be used.
 * @param scene        The scene index to render.
 * @param samplesPixel The number of samples per pixel to use.
 * @param samplesLight The number of samples per light to use.
 * @param repeats      The number of times to render the scene.
 * @param accelerator  The acceleration structure to use.
 * @param printStdOut  Whether or not the logs should be redirected to the standard output.
 * @param async        Whether or not the Ray Tracer should render the image asynchronously or synchronously.
 * @param pathObj      The path to the OBJ file of the scene.
 * @param pathMtl      The path to the MTL file of the scene.
 * @param pathCam      The path to the CAM file of the scene.
 */
void RayTrace(::std::int32_t *const bitmap, const ::std::int32_t width, const ::std::int32_t height, const ::std::int32_t threads,
              const ::std::int32_t shader, const ::std::int32_t scene, const ::std::int32_t samplesPixel, const ::std::int32_t samplesLight,
              const ::std::int32_t repeats, const ::std::int32_t accelerator, const bool printStdOut, const bool async,
              const char *const pathObj, const char *const pathMtl, const char *const pathCam) {
    if (async) {
        ::std::thread thread {work_thread, bitmap, width, height, threads, shader, scene,
                             samplesPixel, samplesLight, repeats, accelerator, printStdOut, pathObj,
                             pathMtl, pathCam};
        thread.detach();
    } else {
        work_thread(bitmap, width, height, threads, shader, scene, samplesPixel, samplesLight,
                    repeats, accelerator, printStdOut, pathObj, pathMtl, pathCam);
    }
}
