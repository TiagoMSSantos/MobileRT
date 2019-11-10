#include "c_wrapper.h"
#include "Components/Cameras/Orthographic.hpp"
#include "Components/Cameras/Perspective.hpp"
#include "Components/Lights/AreaLight.hpp"
#include "Components/Lights/PointLight.hpp"
#include "Components/ObjectLoaders/OBJLoader.hpp"
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

static void
work_thread(
    ::std::uint32_t *const bitmap, const ::std::int32_t width,
    const ::std::int32_t height, const ::std::int32_t threads,
    const ::std::int32_t shader, const ::std::int32_t scene, const ::std::int32_t samplesPixel, const ::std::int32_t samplesLight,
    ::std::int32_t repeats, const ::std::int32_t accelerator, const bool printStdOut,
    const char *const objFileName, const char *const mtlFileName) {
    ::std::ostringstream ss{""};
    ::std::streambuf *old_buf_stdout{nullptr};
    ::std::streambuf *old_buf_stderr{nullptr};
    ::std::chrono::duration<double> timeCreating{0};
    ::std::chrono::duration<double> timeRendering{0};
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
        LOG("pathObj = ", objFileName);
        LOG("pathMtl = ", mtlFileName);

        ::std::unique_ptr<::MobileRT::Renderer> renderer_ {};
        ::std::int32_t numberOfLights_ {0};

        const float ratio {
            ::std::max(static_cast<float>(width) / height, static_cast<float>(height) / width)};
        const float hfovFactor {width > height ? ratio : 1.0F};
        const float vfovFactor {width < height ? ratio : 1.0F};
        ::MobileRT::Scene scene_ {};
        ::std::unique_ptr<::MobileRT::Sampler> samplerPixel {};
        ::std::unique_ptr<::MobileRT::Shader> shader_ {};
        ::std::unique_ptr<::MobileRT::Camera> camera {};
        ::glm::vec3 maxDist{};

        switch (scene) {
            case 0:
                camera = ::std::make_unique<::Components::Perspective>(
                        ::glm::vec3 {0.0F, 0.0F, -3.4F},
                        ::glm::vec3 {0.0F, 0.0F, 1.0F},
                        ::glm::vec3 {0.0F, 1.0F, 0.0F},
                        45.0F * hfovFactor, 45.0F * vfovFactor);
                scene_ = cornellBoxScene(::std::move(scene_));
                maxDist = ::glm::vec3 {1, 1, 1};
                break;

            case 1:
                camera = ::std::make_unique<::Components::Orthographic>(
                        ::glm::vec3 {0.0F, 1.0F, -10.0F},
                        ::glm::vec3 {0.0F, 1.0F, 7.0F},
                        ::glm::vec3 {0.0F, 1.0F, 0.0F},
                        10.0F * hfovFactor, 10.0F * vfovFactor);
                /*camera = ::std::make_unique<::Components::Perspective>(
                  ::glm::vec3 {0.0F, 0.5F, 1.0F},
                  ::glm::vec3 {0.0F, 0.0F, 7.0F},
                  ::glm::vec3 {0.0F, 1.0F, 0.0F},
                  60.0F * hfovFactor, 60.0F * vfovFactor);*/
                scene_ = spheresScene(::std::move(scene_));
                maxDist = ::glm::vec3 {8, 8, 8};
                break;

            case 2:
                camera = ::std::make_unique<::Components::Perspective>(
                        ::glm::vec3 {0.0F, 0.0F, -3.4F},
                        ::glm::vec3 {0.0F, 0.0F, 1.0F},
                        ::glm::vec3 {0.0F, 1.0F, 0.0F},
                        45.0F * hfovFactor, 45.0F * vfovFactor);
                scene_ = cornellBoxScene2(::std::move(scene_));
                maxDist = ::glm::vec3 {1, 1, 1};
                break;

            case 3:
                camera = ::std::make_unique<::Components::Perspective>(
                        ::glm::vec3 {0.0F, 0.5F, 1.0F},
                        ::glm::vec3 {0.0F, 0.0F, 7.0F},
                        ::glm::vec3 {0.0F, 1.0F, 0.0F},
                        60.0F * hfovFactor, 60.0F * vfovFactor);
                scene_ = spheresScene2(::std::move(scene_));
                maxDist = ::glm::vec3 {8, 8, 8};
                break;
            default: {
                ::Components::OBJLoader objLoader {objFileName, mtlFileName};
                objLoader.process();
                if (!objLoader.isProcessed()) {
                    exit(0);
                }
                //objLoader.fillScene (&scene_, []() { return ::std::make_unique<::Components::HaltonSeq> (); });
                //objLoader.fillScene (&scene_, []() {return ::std::make_unique<::Components::MersenneTwister> (); });
                objLoader.fillScene(&scene_, []() { return ::std::make_unique<Components::StaticHaltonSeq>(); });
                //objLoader.fillScene(&scene_, []() { return ::std::make_unique<Components::StaticMersenneTwister>(); });

                const float fovX{45.0F * hfovFactor};
                const float fovY{45.0F * vfovFactor};
                maxDist = ::glm::vec3 {1, 1, 1};
                const ::MobileRT::Material lightMat {::glm::vec3 {0.0F, 0.0F, 0.0F},
                                                     ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                                     ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                                     1.0F,
                                                     ::glm::vec3 {0.9F, 0.9F, 0.9F}};

                //cornellbox
                if (::std::strstr(objFileName, "CornellBox") != nullptr) {
                    camera = ::std::make_unique<Components::Perspective>(
                            ::glm::vec3 {0.0F, 0.7F, 3.0F},
                            ::glm::vec3 {0.0F, 0.7F, -1.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);

                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {0.5F, 1.58F, 0.5F},
                            ::glm::vec3 {-0.5F, 1.58F, 0.5F},
                            ::glm::vec3 {-0.5F, 1.58F, -0.5F}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {0.5F, 1.58F, 0.5F},
                            ::glm::vec3 {-0.5F, 1.58F, -0.5F},
                            ::glm::vec3 {0.5F, 1.58F, -0.5F}));
                }
                
                //conference
                if (::std::strstr(objFileName, "conference") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-100.0F, 640.0F, -100.0F},
                            ::glm::vec3 {100.0F, 640.0F, -100.0F},
                            ::glm::vec3 {100.0F, 640.0F, 100.0F}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-100.0F, 640.0F, -100.0F},
                            ::glm::vec3 {100.0F, 640.0F, 100.0F},
                            ::glm::vec3 {-100.0F, 640.0F, 100.0F}));
                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {460.0F, 500.0F, -1000.0F},
                            ::glm::vec3 {0.0F, 400.0F, 0.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);
                }

                //teapot
                if (::std::strstr(objFileName, "teapot") != nullptr) {
                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0F, 30.0F, -200.0F},
                            ::glm::vec3 {0.0F, 30.0F, 100.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-30.0F, 100.0F, -30.0F},
                            ::glm::vec3 {30.0F, 100.0F, -30.0F},
                            ::glm::vec3 {30.0F, 100.0F, 30.0F}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-30.0F, 100.0F, -30.0F},
                            ::glm::vec3 {30.0F, 100.0F, 30.0F},
                            ::glm::vec3 {-30.0F, 100.0F, 30.0F}));
                }

                //dragon
                if (::std::strstr(objFileName, "dragon") != nullptr) {
                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0F, 0.0F, -1.5F},
                            ::glm::vec3 {0.0F, 0.0F, 0.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, 0.3F}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, 0.3F},
                            ::glm::vec3 {-0.3F, 1.0F, 0.3F}));
                }

                //bedroom
                if (::std::strstr(objFileName, "bedroom") != nullptr) {
                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0F, 0.0F, -2.5F},
                            ::glm::vec3 {0.0F, 0.0F, 0.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, 0.3F}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, 0.3F},
                            ::glm::vec3 {-0.3F, 1.0F, 0.3F}));
                }

                //breakfast_room
                if (::std::strstr(objFileName, "breakfast_room") != nullptr) {
                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0F, 0.0F, -5.0F},
                            ::glm::vec3 {0.0F, 0.0F, 0.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, 0.3F}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, 0.3F},
                            ::glm::vec3 {-0.3F, 1.0F, 0.3F}));
                }

                //buddha
                if (::std::strstr(objFileName, "buddha") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, 0.3F}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, 0.3F},
                            ::glm::vec3 {-0.3F, 1.0F, 0.3F}));

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0F, 0.0F, -2.5F},
                            ::glm::vec3 {0.0F, 0.0F, 0.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);
                }

                //erato
                if (::std::strstr(objFileName, "erato") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, 0.3F}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, 0.3F},
                            ::glm::vec3 {-0.3F, 1.0F, 0.3F}));

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0F, 0.0F, -2.5F},
                            ::glm::vec3 {0.0F, 0.0F, 0.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);
                }

                //gallery
                if (::std::strstr(objFileName, "gallery") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, 0.3F}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.3F, 1.0F, -0.3F},
                            ::glm::vec3 {0.3F, 1.0F, 0.3F},
                            ::glm::vec3 {-0.3F, 1.0F, 0.3F}));

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0F, 0.0F, -2.5F},
                            ::glm::vec3 {0.0F, 0.0F, 0.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);
                }

                //Porsche
                if (::std::strstr(objFileName, "Porsche") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-1.0F, 1.0F, 1.0F},
                            ::glm::vec3 {1.0F, 1.0F, -1.0F},
                            ::glm::vec3 {1.0F, 1.0F, 1.0F}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-1.0F, 1.0F, 1.0F},
                            ::glm::vec3 {-1.0F, 1.0F, -1.0F},
                            ::glm::vec3 {1.0F, 1.0F, -1.0F}));

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {-4.0F, 2.0F, -2.5F},
                            ::glm::vec3 {0.0F, 0.0F, 0.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);
                }
            }
                break;
        }
        if (samplesPixel > 1) {
            //samplerPixel = ::std::make_unique<::Components::HaltonSeq> ();
            //samplerPixel = ::std::make_unique<::Components::Stratified> ();
            samplerPixel = ::std::make_unique<::Components::StaticHaltonSeq>();
        } else {
            samplerPixel = ::std::make_unique<::Components::Constant>(0.5F);
        }
        switch (shader) {
            case 1: {
                shader_ = ::std::make_unique<::Components::Whitted>(::std::move(scene_),
                                                                    samplesLight,
                                                                    ::MobileRT::Shader::Accelerator(
                                                                            accelerator));
                break;
            }

            case 2: {
                //::std::unique_ptr<::MobileRT::Sampler> samplerRussianRoulette {::std::make_unique<::Components::HaltonSeq>()};
                //::std::unique_ptr<::MobileRT::Sampler> samplerRussianRoulette {::std::make_unique<::Components::MersenneTwister> ()};
                ::std::unique_ptr<MobileRT::Sampler> samplerRussianRoulette {::std::make_unique<::Components::StaticHaltonSeq>()};
                //::std::unique_ptr<MobileRT::Sampler> samplerRussianRoulette {::std::make_unique<::Components::StaticMersenneTwister> ()};

                shader_ = ::std::make_unique<::Components::PathTracer>(
                        ::std::move(scene_), ::std::move(samplerRussianRoulette), samplesLight,
                        ::MobileRT::Shader::Accelerator(accelerator));
                break;
            }

            case 3: {
                shader_ = ::std::make_unique<::Components::DepthMap>(::std::move(scene_), maxDist,
                                                                     ::MobileRT::Shader::Accelerator(
                                                                             accelerator));
                break;
            }

            case 4: {
                shader_ = ::std::make_unique<::Components::DiffuseMaterial>(::std::move(scene_),
                                                                            ::MobileRT::Shader::Accelerator(
                                                                                    accelerator));
                break;
            }

            default: {
                shader_ = ::std::make_unique<::Components::NoShadows>(::std::move(scene_),
                                                                      samplesLight,
                                                                      ::MobileRT::Shader::Accelerator(
                                                                              accelerator));
                break;
            }
        }
        const ::std::int32_t triangles{static_cast<::std::int32_t> (shader_->scene_.triangles_.size())};
        const ::std::int32_t spheres{static_cast<::std::int32_t> (shader_->scene_.spheres_.size())};
        const ::std::int32_t planes{static_cast<::std::int32_t> (shader_->scene_.planes_.size())};
        numberOfLights_ = static_cast<::std::int32_t> (shader_->scene_.lights_.size());
        const ::std::int32_t nPrimitives = triangles + spheres + planes;

        LOG("Started creating Renderer");
        const auto startCreating{::std::chrono::system_clock::now()};
        renderer_ = ::std::make_unique<::MobileRT::Renderer>(
                ::std::move(shader_), ::std::move(camera), ::std::move(samplerPixel),
                static_cast<::std::uint32_t>(width), static_cast<::std::uint32_t>(height),
                static_cast<::std::uint32_t>(samplesPixel));
        const auto endCreating{::std::chrono::system_clock::now()};
        timeCreating = endCreating - startCreating;
        LOG("Renderer created = ", timeCreating.count());

        LOG("TRIANGLES = ", triangles);
        LOG("SPHERES = ", spheres);
        LOG("PLANES = ", planes);
        LOG("PRIMITIVES = ", nPrimitives);
        LOG("LIGHTS = ", numberOfLights_);
        LOG("threads = ", threads);
        LOG("shader = ", shader);
        LOG("scene = ", scene);
        LOG("samplesPixel = ", samplesPixel);
        LOG("samplesLight = ", samplesLight);
        LOG("width_ = ", width);
        LOG("height_ = ", height);

        LOG("Started rendering scene");
        const auto startRendering{::std::chrono::system_clock::now()};
        do {
            renderer_->renderFrame(bitmap, threads, width * sizeof(::std::uint32_t));
            repeats--;
        } while (repeats > 0);
        const auto endRendering{::std::chrono::system_clock::now()};
        timeRendering = endRendering - startRendering;
        LOG("Finished rendering scene");
    }
    if (!printStdOut) {
        ::std::cout.rdbuf(old_buf_stdout);
        ::std::cerr.rdbuf(old_buf_stderr);
    }

    LOG("Creating Time in secs = ", timeCreating.count());
    LOG("Rendering Time in secs = ", timeRendering.count());
}

void RayTrace(::std::uint32_t *const bitmap, const ::std::int32_t width, const ::std::int32_t height, const ::std::int32_t threads,
              const ::std::int32_t shader, const ::std::int32_t scene, const ::std::int32_t samplesPixel, const ::std::int32_t samplesLight,
              const ::std::int32_t repeats, const ::std::int32_t accelerator, const bool printStdOut, const bool async,
              const char *const pathObj, const char *const pathMtl) {
    if (async) {
        ::std::thread thread {work_thread, bitmap, width, height, threads, shader, scene,
                             samplesPixel, samplesLight, repeats, accelerator, printStdOut, pathObj,
                             pathMtl};
        thread.detach();
    } else {
        work_thread(bitmap, width, height, threads, shader, scene, samplesPixel, samplesLight,
                    repeats, accelerator, printStdOut, pathObj, pathMtl);
    }
}
