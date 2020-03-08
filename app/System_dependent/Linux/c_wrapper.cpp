#include "c_wrapper.h"
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
#include "Scenes.hpp"
#include <chrono>
#include <cstring>
#include <fstream>

static ::std::unique_ptr<::MobileRT::Renderer> renderer_ {};

static void
work_thread(
    ::std::int32_t *const bitmap, const ::std::int32_t width,
    const ::std::int32_t height, const ::std::int32_t threads,
    const ::std::int32_t shader, const ::std::int32_t scene, const ::std::int32_t samplesPixel, const ::std::int32_t samplesLight,
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
            old_buf_stdout = ::std::cout.rdbuf(ss.rdbuf());
            old_buf_stderr = ::std::cerr.rdbuf(ss.rdbuf());
        }
        {
            LOG("width_ = ", width);
            LOG("height_ = ", height);
            LOG("threads = ", threads);
            LOG("shader = ", shader);
            LOG("scene = ", scene);
            LOG("samplesPixel = ", samplesPixel);
            LOG("samplesLight = ", samplesLight);
            LOG("repeats = ", repeats);
            LOG("accelerator = ", accelerator);
            LOG("printStdOut = ", printStdOut);
            LOG("objFilePath = ", objFilePath);
            LOG("mtlFilePath = ", mtlFilePath);
            LOG("camFilePath = ", camFilePath);

            const auto ratio {static_cast<float> (width) / height};
            ::MobileRT::Scene scene_ {};
            ::std::unique_ptr<::MobileRT::Sampler> samplerPixel {};
            ::std::unique_ptr<::MobileRT::Shader> shader_ {};
            ::std::unique_ptr<::MobileRT::Camera> camera {};
            ::glm::vec3 maxDist {};

            switch (scene) {
                case 0:
                    camera = ::std::make_unique<::Components::Perspective> (
                            ::glm::vec3 {0.0F, 0.0F, -3.4F},
                            ::glm::vec3 {0.0F, 0.0F, 1.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            45.0F * ratio, 45.0F);
                    scene_ = cornellBoxScene(::std::move(scene_));
                    maxDist = ::glm::vec3 {1, 1, 1};
                    break;

                case 1:
                    camera = ::std::make_unique<::Components::Orthographic> (
                            ::glm::vec3 {0.0F, 1.0F, -10.0F},
                            ::glm::vec3 {0.0F, 1.0F, 7.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            10.0F * ratio, 10.0F);
                    scene_ = spheresScene(::std::move(scene_));
                    maxDist = ::glm::vec3 {8, 8, 8};
                    break;

                case 2:
                    camera = ::std::make_unique<::Components::Perspective> (
                            ::glm::vec3 {0.0F, 0.0F, -3.4F},
                            ::glm::vec3 {0.0F, 0.0F, 1.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            45.0F * ratio, 45.0F);
                    scene_ = cornellBoxScene2(::std::move(scene_));
                    maxDist = ::glm::vec3 {1, 1, 1};
                    break;

                case 3:
                    camera = ::std::make_unique<::Components::Perspective> (
                            ::glm::vec3 {0.0F, 0.5F, 1.0F},
                            ::glm::vec3 {0.0F, 0.0F, 7.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            60.0F * ratio, 60.0F);
                    scene_ = spheresScene2(::std::move(scene_));
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
                    LOG("OBJLoader loaded = ", timeLoading.count());
                    const auto startFilling {::std::chrono::system_clock::now()};
                    //objLoader.fillScene(&scene_, []() {return ::std::make_unique<::Components::HaltonSeq> ();});
                    //objLoader.fillScene(&scene_, []() {return ::std::make_unique<::Components::MersenneTwister> ();});
                    objLoader.fillScene(&scene_, []() {return ::std::make_unique<Components::StaticHaltonSeq> (); });
                    //objLoader.fillScene(&scene_, []() {return ::std::make_unique<Components::StaticMersenneTwister> ();});
                    const auto endFilling {::std::chrono::system_clock::now()};
                    timeFilling = endFilling - startFilling;
                    LOG("Scene filled = ", timeFilling.count());

                    const auto cameraFactory {::Components::CameraFactory()};
                    camera = cameraFactory.loadFromFile(camFilePath, ratio);
                    maxDist = ::glm::vec3 {1, 1, 1};
                }
                    break;
            }
            if (samplesPixel > 1) {
                samplerPixel = ::std::make_unique<::Components::StaticHaltonSeq> ();
            } else {
                samplerPixel = ::std::make_unique<::Components::Constant> (0.5F);
            }
            const auto startCreating {::std::chrono::system_clock::now()};
            switch (shader) {
                case 1: {
                    shader_ = ::std::make_unique<::Components::Whitted> (
                    ::std::move(scene_), samplesLight, ::MobileRT::Shader::Accelerator(accelerator)
                    );
                    break;
                }

                case 2: {
                    ::std::unique_ptr<MobileRT::Sampler> samplerRussianRoulette {
                        ::std::make_unique<::Components::StaticHaltonSeq> ()
                    };

                    shader_ = ::std::make_unique<::Components::PathTracer> (
                    ::std::move(scene_), ::std::move(samplerRussianRoulette), samplesLight,
                    ::MobileRT::Shader::Accelerator(accelerator)
                    );
                    break;
                }

                case 3: {
                shader_ = ::std::make_unique<::Components::DepthMap> (
                    ::std::move(scene_), maxDist, ::MobileRT::Shader::Accelerator(accelerator)
                );
                    break;
                }

                case 4: {
                shader_ = ::std::make_unique<::Components::DiffuseMaterial> (
                    ::std::move(scene_), ::MobileRT::Shader::Accelerator(accelerator)
                );
                break;
                }

                default: {
                shader_ = ::std::make_unique<::Components::NoShadows> (
                    ::std::move(scene_), samplesLight, ::MobileRT::Shader::Accelerator(accelerator)
                );
                break;
                }
            }
            const auto endCreating {::std::chrono::system_clock::now()};
            timeCreating = endCreating - startCreating;
            LOG("Shader created = ", timeCreating.count());

            const auto planes {static_cast<::std::int32_t> (shader_->getPlanes().size())};
            const auto spheres {static_cast<::std::int32_t> (shader_->getSpheres().size())};
            const auto triangles {static_cast<::std::int32_t> (shader_->getTriangles().size())};
            const auto numLights {static_cast<::std::int32_t> (shader_->getLights().size())};
            const auto nPrimitives {triangles + spheres + planes};

            LOG("Started creating Renderer");
            renderer_ = ::std::make_unique<::MobileRT::Renderer> (
                    ::std::move(shader_), ::std::move(camera), ::std::move(samplerPixel),
                    width, height, samplesPixel
            );

            LOG("TRIANGLES = ", triangles);
            LOG("SPHERES = ", spheres);
            LOG("PLANES = ", planes);
            LOG("PRIMITIVES = ", nPrimitives);
            LOG("LIGHTS = ", numLights);
            LOG("threads = ", threads);
            LOG("shader = ", shader);
            LOG("scene = ", scene);
            LOG("samplesPixel = ", samplesPixel);
            LOG("samplesLight = ", samplesLight);
            LOG("width_ = ", width);
            LOG("height_ = ", height);

            LOG("Started rendering scene");
            const auto startRendering {::std::chrono::system_clock::now()};
            do {
                renderer_->renderFrame(bitmap, threads);
                repeats--;
            } while (repeats > 0);
            const auto endRendering {::std::chrono::system_clock::now()};

            timeRendering = endRendering - startRendering;
            LOG("Finished rendering scene");
        }
        if (!printStdOut) {
            ::std::cout.rdbuf(old_buf_stdout);
            ::std::cerr.rdbuf(old_buf_stderr);
        }

        LOG("Loading Time in secs = ", timeLoading.count());
        LOG("Filling Time in secs = ", timeFilling.count());
        LOG("Creating Time in secs = ", timeCreating.count());
        LOG("Rendering Time in secs = ", timeRendering.count());
    } catch (const ::std::bad_alloc &badAlloc) {
        LOG("badAlloc: ", badAlloc.what());
    } catch (const ::std::exception &exception) {
        LOG("exception: ", exception.what());
    } catch (...) {
        LOG("Unknown error");
    }
}

void stopRender() {
    if (renderer_ != nullptr) {
        renderer_->stopRender();
    }
}

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
