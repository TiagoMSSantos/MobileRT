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
#include "MobileRT/Config.hpp"
#include "MobileRT/Renderer.hpp"
#include "MobileRT/Scene.hpp"
#include "Scenes/Scenes.hpp"

#include <chrono>
#include <cstring>
#include <fstream>
#include <functional>

static ::std::unique_ptr<::MobileRT::Renderer> renderer_ {};

/**
 * Helper method that starts the Ray Tracer engine.
 *
 * @param config The MobileRT configurator.
 */
static void work_thread(::MobileRT::Config &config) {
    // Reset all errors due to Qt.
    errno = 0;
    try {
        ::std::chrono::duration<double> timeCreating {};
        ::std::chrono::duration<double> timeRendering {};
        ::std::chrono::duration<double> timeLoading {};
        ::std::chrono::duration<double> timeFilling {};
        {
            // Print debug information
            LOG_DEBUG("width = ", config.width);
            LOG_DEBUG("height = ", config.height);
            LOG_DEBUG("threads = ", config.threads);
            LOG_DEBUG("shader = ", config.shader);
            LOG_DEBUG("scene = ", config.sceneIndex);
            LOG_DEBUG("samplesPixel = ", config.samplesPixel);
            LOG_DEBUG("samplesLight = ", config.samplesLight);
            LOG_DEBUG("repeats = ", config.repeats);
            LOG_DEBUG("accelerator = ", config.accelerator);
            LOG_DEBUG("objFilePath = ", config.objFilePath);
            LOG_DEBUG("mtlFilePath = ", config.mtlFilePath);
            LOG_DEBUG("camFilePath = ", config.camFilePath);

            const float ratio {static_cast<float> (config.width) / config.height};
            ::MobileRT::Scene scene {};
            ::std::unique_ptr<::MobileRT::Sampler> samplerPixel {};
            ::std::unique_ptr<::MobileRT::Shader> shader_ {};
            ::std::unique_ptr<::MobileRT::Camera> camera {};
            ::glm::vec3 maxDist {};

            // Setup scene and camera
            switch (config.sceneIndex) {
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
                    camera = cornellBox_Cam(ratio);
                    maxDist = ::glm::vec3{1, 1, 1};
                    break;

                case 3:
                    scene = spheres2_Scene(::std::move(scene));
                    camera = spheres2_Cam(ratio);
                    maxDist = ::glm::vec3 {8, 8, 8};
                    break;

                default: {
                    LOG_DEBUG("OBJLoader starting loading scene");
                    const ::std::chrono::time_point<::std::chrono::system_clock> chronoStartLoading {::std::chrono::system_clock::now()};
                    ::std::ifstream ifObj {config.objFilePath};
                    ::std::ifstream ifMtl {config.mtlFilePath};
                    ::Components::OBJLoader objLoader {ifObj, ifMtl};
                    if (!objLoader.isProcessed()) {
                        LOG_ERROR("Error occurred while loading scene.");
                        exit(1);
                    }
                    const ::std::chrono::time_point<::std::chrono::system_clock> chronoEndLoading {::std::chrono::system_clock::now()};
                    timeLoading = chronoEndLoading - chronoStartLoading;
                    ::std::unordered_map<::std::string, ::MobileRT::Texture> texturesCache {};
                    LOG_INFO("OBJLoader loaded = ", ::std::chrono::duration_cast<::std::chrono::seconds>(timeLoading).count(), " seconds");
                    const ::std::chrono::time_point<::std::chrono::system_clock> chronoStartFilling {::std::chrono::system_clock::now()};
                    const bool sceneBuilt {objLoader.fillScene(
                        &scene,
                        []() { return ::MobileRT::std::make_unique<Components::StaticHaltonSeq> (); },
                        config.objFilePath,
                        texturesCache
                    )};
                    if (!sceneBuilt) {
                        LOG_ERROR("OBJLOADER could not load the scene.");
                        return;
                    }
                    ::MobileRT::checkSystemError("Filled Scene.");
                    const ::std::chrono::time_point<::std::chrono::system_clock> chronoEndFilling {::std::chrono::system_clock::now()};
                    timeFilling = chronoEndFilling - chronoStartFilling;
                    texturesCache.clear();
                    LOG_INFO("Scene filled = ", ::std::chrono::duration_cast<::std::chrono::seconds>(timeFilling).count(), " seconds");

                    ::Components::CameraFactory cameraFactory {::Components::CameraFactory()};
                    ::std::ifstream ifCamera {config.camFilePath};
                    ::std::istream iCam {ifCamera.rdbuf()};
                    ::MobileRT::checkSystemError("Loading Camera file.");
                    camera = cameraFactory.loadFromFile(iCam, ratio);
                    ::MobileRT::checkSystemError("Loaded Camera file.");
                    maxDist = ::glm::vec3 {1, 1, 1};
                }
                    break;
            }
            // Setup sampler
            ::MobileRT::checkSystemError("Creating Sampler.");
            if (config.samplesPixel > 1) {
                samplerPixel = ::MobileRT::std::make_unique<::Components::StaticHaltonSeq> ();
            } else {
                samplerPixel = ::MobileRT::std::make_unique<::Components::Constant> (0.5F);
            }
            ::MobileRT::checkSystemError("Starting creating shader");
            // Start timer to measure latency of creating shader (including the build of
            // acceleration structure)
            const ::std::chrono::time_point<::std::chrono::system_clock> chronoStartCreating {::std::chrono::system_clock::now()};
            // Setup shader
            switch (config.shader) {
                case 1: {
                    shader_ = ::MobileRT::std::make_unique<::Components::Whitted> (
                    ::std::move(scene), config.samplesLight, ::MobileRT::Shader::Accelerator(config.accelerator)
                    );
                    break;
                }

                case 2: {
                    ::std::unique_ptr<MobileRT::Sampler> samplerRussianRoulette {
                            ::MobileRT::std::make_unique<::Components::StaticHaltonSeq> ()
                    };

                    shader_ = ::MobileRT::std::make_unique<::Components::PathTracer> (
                    ::std::move(scene), ::std::move(samplerRussianRoulette), config.samplesLight,
                    ::MobileRT::Shader::Accelerator(config.accelerator)
                    );
                    break;
                }

                case 3: {
                shader_ = ::MobileRT::std::make_unique<::Components::DepthMap> (
                    ::std::move(scene), maxDist, ::MobileRT::Shader::Accelerator(config.accelerator)
                );
                    break;
                }

                case 4: {
                shader_ = ::MobileRT::std::make_unique<::Components::DiffuseMaterial> (
                    ::std::move(scene), ::MobileRT::Shader::Accelerator(config.accelerator)
                );
                break;
                }

                default: {
                shader_ = ::MobileRT::std::make_unique<::Components::NoShadows> (
                    ::std::move(scene), config.samplesLight, ::MobileRT::Shader::Accelerator(config.accelerator)
                );
                break;
                }
            }
            // Stop timer
            const ::std::chrono::time_point<::std::chrono::system_clock> chronoEndCreating {::std::chrono::system_clock::now()};
            ::MobileRT::checkSystemError("Created shader");
            timeCreating = chronoEndCreating - chronoStartCreating;
            LOG_INFO("TRIANGLES = ", static_cast<::std::int32_t> (shader_->getTriangles().size()));
            LOG_INFO("LIGHTS = ", static_cast<::std::int32_t> (shader_->getLights().size()));
            LOG_DEBUG("SPHERES = ", static_cast<::std::int32_t> (shader_->getSpheres().size()));
            LOG_DEBUG("PLANES = ", static_cast<::std::int32_t> (shader_->getPlanes().size()));
            LOG_DEBUG("Shader created = ", timeCreating.count(), " secs");

            ::MobileRT::checkSystemError("Starting creating renderer");
            LOG_INFO("Started creating Renderer");
            renderer_ = ::MobileRT::std::make_unique<::MobileRT::Renderer> (
                    ::std::move(shader_), ::std::move(camera), ::std::move(samplerPixel),
                    config.width, config.height, config.samplesPixel
            );
            ::MobileRT::checkSystemError("Created renderer");

            // Print debug information
            LOG_INFO("threads = ", config.threads);
            LOG_INFO("shader = ", config.shader);
            LOG_INFO("scene = ", config.sceneIndex);
            LOG_INFO("samplesPixel = ", config.samplesPixel);
            LOG_INFO("samplesLight = ", config.samplesLight);
            LOG_INFO("width = ", config.width);
            LOG_INFO("height = ", config.height);
            LOG_INFO("repeats = ", config.repeats);
            LOG_INFO("accelerator = ", config.accelerator);

            ::std::int32_t repeats {config.repeats};
            ::MobileRT::checkSystemError("Starting rendering");
            LOG_INFO("Started rendering scene");
            const ::std::chrono::time_point<::std::chrono::system_clock> chronoStartRendering {::std::chrono::system_clock::now()};
            do {
                // Render a frame
                renderer_->renderFrame(config.bitmap.data(), config.threads);
                repeats--;
            } while (repeats > 0);
            const ::std::chrono::time_point<::std::chrono::system_clock> chronoEndRendering {::std::chrono::system_clock::now()};
            ::MobileRT::checkSystemError("Rendering ended");

            timeRendering = chronoEndRendering - chronoStartRendering;
            LOG_INFO("Finished rendering scene");
        }

        // Print some latencies
        const double renderingTime {timeRendering.count()};
        const ::std::uint64_t castedRays {renderer_->getTotalCastedRays()};
        LOG_INFO("Loading Time in secs = ", timeLoading.count());
        LOG_INFO("Filling Time in secs = ", timeFilling.count());
        LOG_INFO("Creating Time in secs = ", timeCreating.count());
        LOG_INFO("Rendering Time in secs = ", renderingTime);
        LOG_INFO("Casted rays = ", castedRays);
        LOG_INFO("width = ", config.width);
        LOG_INFO("height = ", config.height);

        LOG_INFO("Total Millions rays per second = ", (static_cast<double> (castedRays) / renderingTime) / 1000000L);
    } catch (const ::std::bad_alloc &badAlloc) {
        LOG_ERROR("badAlloc: ", badAlloc.what());
    } catch (const ::std::exception &exception) {
        LOG_ERROR("exception: ", exception.what());
    } catch (...) {
        LOG_ERROR("Unknown error");
    }
    // Force the calling Ray Tracing engine destructors, which is useful for the unit tests.
    renderer_.reset(nullptr);
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
 * @param config The MobileRT configurator.
 * @param async  Whether the Ray Tracer should render the image asynchronously or synchronously.
 */
void RayTrace(::MobileRT::Config &config, const bool async) {
    if (async) {
        ::std::thread thread {work_thread, ::std::ref(config)};
        thread.detach();
    } else {
        work_thread(config);
    }
}
