#include "JNI_layer.hpp"

#include "Components/Cameras/Orthographic.hpp"
#include "Components/Cameras/Perspective.hpp"
#include "Components/Lights/AreaLight.hpp"
#include "Components/Lights/PointLight.hpp"
#include "Components/Loaders/CameraFactory.hpp"
#include "Components/Loaders/OBJLoader.hpp"
#include "Components/Loaders/PerspectiveLoader.hpp"
#include "Components/Samplers/Constant.hpp"
#include "Components/Samplers/HaltonSeq.hpp"
#include "Components/Samplers/MersenneTwister.hpp"
#include "Components/Samplers/PCG.hpp"
#include "Components/Samplers/StaticHaltonSeq.hpp"
#include "Components/Samplers/StaticMersenneTwister.hpp"
#include "Components/Samplers/StaticPCG.hpp"
#include "Components/Samplers/Stratified.hpp"
#include "Components/Shaders/DepthMap.hpp"
#include "Components/Shaders/DiffuseMaterial.hpp"
#include "Components/Shaders/NoShadows.hpp"
#include "Components/Shaders/PathTracer.hpp"
#include "Components/Shaders/Whitted.hpp"
#include "MobileRT/Renderer.hpp"
#include "MobileRT/Scene.hpp"
#include "Scenes/Scenes.hpp"

#include <android/bitmap.h>
#include <condition_variable>
#include <glm/glm.hpp>
#include <fstream>
#include <mutex>
#include <string>

/**
 * The number of frames per second.
 */
static float fps_ {};

/**
 * The current state of the MobileRT engine.
 */
static ::std::atomic<::State> state_ {State::IDLE};

/**
 * The MobileRT Renderer.
 */
static ::std::unique_ptr<::MobileRT::Renderer> renderer_ {};

/**
 * A Java Virtual Machine.
 */
static ::std::unique_ptr<::JavaVM> javaVM_ {};

/**
 * The auxiliary thread to execute the MobileRT render.
 */
static ::std::unique_ptr<::std::thread> thread_ {};

/**
 * The mutex for the MobileRT to only 1 thread execute it.
 */
static ::std::mutex mutex_ {};

/**
 * The number of lights in the current scene.
 */
static ::std::int32_t numLights_ {};

/**
 * The elapsed time in milliseconds to create the Shader and thus the Acceleration Structure.
 */
static ::std::int64_t timeRenderer_ {};

/**
 * The condition variable to wait for the MobileRT Renderer to finish the rendering process.
 */
static ::std::condition_variable rendered_ {};

/**
 * Whether or not the rendering process was finished.
 */
static ::std::atomic<bool> finishedRendering_ {true};


/**
 * Helper method that throws a Java exception.
 *
 * @param env           The JNI environment.
 * @param exception     The exception which contains the message to add.
 * @param exceptionName The name of the exception class to throw.
 */
static void handleException(JNIEnv *const env,
                            const ::std::exception &exception,
                            const char *const exceptionName) {
    const auto clazz {env->FindClass(exceptionName)};
    const auto res {env->ThrowNew(clazz, exception.what())};
    if (res != 0) {
        LOG_ERROR("ERROR: ", res);
    } else {
        LOG_ERROR(exceptionName, " thrown");
    }
    state_ = State::IDLE;
    finishedRendering_ = true;
}

extern "C"
::std::int32_t JNI_OnLoad(JavaVM *const jvm, void * /*reserved*/) {
    // Reset errno to avoid "errno EINVAL (22): Invalid argument".
    errno = 0;
    MobileRT::checkSystemError("JNI_OnLoad start");
    LOG_DEBUG("JNI_OnLoad");
    javaVM_.reset(jvm);

    JNIEnv *jniEnv {};
    {
        const ::std::int32_t result {javaVM_->GetEnv(reinterpret_cast<void **> (&jniEnv), JNI_VERSION_1_6)};
        ASSERT(result == JNI_OK, "JNI was not loaded properly.");
        static_cast<void> (result);
    }
    ASSERT(jniEnv != nullptr, "JNIEnv was not loaded properly.");
    jniEnv->ExceptionClear();
    MobileRT::checkSystemError("JNI_OnLoad finish");
    return JNI_VERSION_1_6;
}

extern "C"
void JNI_OnUnload(JavaVM *const /*jvm*/, void * /*reserved*/) {
    LOG_DEBUG("JNI_OnUnload");
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_rtInitCameraArray(
    JNIEnv *env,
    jobject /*thiz*/
) {
    MobileRT::checkSystemError("rtInitCameraArray start");
    try {
        jobject directBuffer {};
        {
            const ::std::lock_guard<::std::mutex> lock {mutex_};
            if (renderer_ != nullptr) {
                ::MobileRT::Camera *const camera {renderer_->camera_.get()};
                const ::std::int64_t arraySize {20};
                const auto arrayBytes {arraySize * sizeof(jfloat)};

                float *const floatBuffer {new float[arraySize]};

                if (floatBuffer != nullptr) {
                    directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);
                    if (directBuffer != nullptr) {
                        ::std::int32_t i{};

                        floatBuffer[i++] = camera->position_.x;
                        floatBuffer[i++] = camera->position_.y;
                        floatBuffer[i++] = camera->position_.z;
                        floatBuffer[i++] = 1.0F;

                        floatBuffer[i++] = camera->direction_.x;
                        floatBuffer[i++] = camera->direction_.y;
                        floatBuffer[i++] = camera->direction_.z;
                        floatBuffer[i++] = 1.0F;

                        floatBuffer[i++] = camera->up_.x;
                        floatBuffer[i++] = camera->up_.y;
                        floatBuffer[i++] = camera->up_.z;
                        floatBuffer[i++] = 1.0F;

                        floatBuffer[i++] = camera->right_.x;
                        floatBuffer[i++] = camera->right_.y;
                        floatBuffer[i++] = camera->right_.z;
                        floatBuffer[i++] = 1.0F;

                        const auto *const perspective {dynamic_cast<::Components::Perspective *> (camera)};
                        const auto *const orthographic {dynamic_cast<::Components::Orthographic *> (camera)};
                        if (perspective != nullptr) {
                            const float hFov{perspective->getHFov()};
                            const float vFov{perspective->getVFov()};
                            floatBuffer[i++] = hFov;
                            floatBuffer[i++] = vFov;
                            floatBuffer[i++] = 0.0F;
                            floatBuffer[i++] = 0.0F;
                        }
                        if (orthographic != nullptr) {
                            const float sizeH{orthographic->getSizeH()};
                            const float sizeV{orthographic->getSizeV()};
                            floatBuffer[i++] = 0.0F;
                            floatBuffer[i++] = 0.0F;
                            floatBuffer[i++] = sizeH;
                            floatBuffer[i] = sizeV;
                        }
                    }
                }

            }
            env->ExceptionClear();
        }
        MobileRT::checkSystemError("rtInitCameraArray finish");
        return directBuffer;
    } catch (const ::std::bad_alloc &badAlloc) {
        handleException(env, badAlloc, "puscas/mobilertapp/exceptions/LowMemoryException");
    } catch (const ::std::exception &exception) {
        handleException(env, exception, "java/lang/RuntimeException");
    } catch (...) {
        handleException(env, ::std::exception{}, "java/lang/RuntimeException");
    }
    MobileRT::checkSystemError("rtInitCameraArray finish");
    return nullptr;
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_rtInitVerticesArray(
    JNIEnv *env,
    jobject /*thiz*/
) {
    try {
        MobileRT::checkSystemError("rtInitVerticesArray start");
        jobject directBuffer {};
        {
            const ::std::lock_guard<::std::mutex> lock {mutex_};
            if (renderer_ != nullptr) {
                const auto &triangles {renderer_->shader_->getTriangles()};
                const auto arraySize {static_cast<::std::uint32_t> (triangles.size() * 3 * 4)};
                const auto arrayBytes {arraySize * static_cast<jlong> (sizeof(jfloat))};

                float *const floatBuffer {new float[arraySize]};

                if (floatBuffer != nullptr) {
                    directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);

                    if (directBuffer != nullptr) {
                        ::std::int32_t i {};
                        for (const auto &triangle : triangles) {
                            const ::glm::vec4 &pointA {triangle.getA().x,
                                                      triangle.getA().y,
                                                      triangle.getA().z, 1.0F};
                            const ::glm::vec4 &pointB {pointA.x + triangle.getAB().x,
                                                      pointA.y + triangle.getAB().y,
                                                      pointA.z + triangle.getAB().z, 1.0F};
                            const ::glm::vec4 &pointC {pointA.x + triangle.getAC().x,
                                                      pointA.y + triangle.getAC().y,
                                                      pointA.z + triangle.getAC().z, 1.0F};

                            floatBuffer[i++] = pointA.x;
                            floatBuffer[i++] = pointA.y;
                            floatBuffer[i++] = -pointA.z;
                            floatBuffer[i++] = pointA.w;

                            floatBuffer[i++] = pointB.x;
                            floatBuffer[i++] = pointB.y;
                            floatBuffer[i++] = -pointB.z;
                            floatBuffer[i++] = pointB.w;

                            floatBuffer[i++] = pointC.x;
                            floatBuffer[i++] = pointC.y;
                            floatBuffer[i++] = -pointC.z;
                            floatBuffer[i++] = pointC.w;
                        }
                    }

                }
            }
            env->ExceptionClear();
        }
        MobileRT::checkSystemError("rtInitVerticesArray finish");
        return directBuffer;
    } catch (const ::std::bad_alloc &badAlloc) {
        handleException(env, badAlloc, "puscas/mobilertapp/exceptions/LowMemoryException");
    } catch (const ::std::exception &exception) {
        handleException(env, exception, "java/lang/RuntimeException");
    } catch (...) {
        handleException(env, ::std::exception{}, "java/lang/RuntimeException");
    }
    MobileRT::checkSystemError("rtInitVerticesArray finish");
    return nullptr;
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_rtInitColorsArray(
    JNIEnv *env,
    jobject /*thiz*/
) {
    MobileRT::checkSystemError("rtInitColorsArray start");
    try {
        jobject directBuffer{};
        {
            const ::std::lock_guard<::std::mutex> lock {mutex_};
            if (renderer_ != nullptr) {
                const auto &triangles {renderer_->shader_->getTriangles()};
                const auto arraySize {static_cast<::std::uint32_t> (triangles.size() * 3 * 4)};
                const auto arrayBytes {arraySize * static_cast<::std::int64_t> (sizeof(jfloat))};

                float *const floatBuffer {new float[arraySize]};

                if (floatBuffer != nullptr) {
                    directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);
                    if (directBuffer != nullptr) {
                        ::std::int32_t i {};
                        for (const auto &triangle : triangles) {
                            const auto materialIndex{triangle.getMaterialIndex()};
                            auto material{::MobileRT::Material{}};
                            if (materialIndex >= 0) {
                                material = renderer_->shader_->getMaterials()
                                [static_cast<::std::uint32_t> (materialIndex)];
                            }

                            const auto &kD{material.Kd_};
                            const auto &kS{material.Ks_};
                            const auto &kT{material.Kt_};
                            const auto &lE{material.Le_};
                            auto color{kD};

                            color = ::glm::all(::glm::greaterThan(kS, color)) ? kS : color;
                            color = ::glm::all(::glm::greaterThan(kT, color)) ? kT : color;
                            color = ::glm::all(::glm::greaterThan(lE, color)) ? lE : color;

                            floatBuffer[i++] = color.r;
                            floatBuffer[i++] = color.g;
                            floatBuffer[i++] = color.b;
                            floatBuffer[i++] = 1.0F;

                            floatBuffer[i++] = color.r;
                            floatBuffer[i++] = color.g;
                            floatBuffer[i++] = color.b;
                            floatBuffer[i++] = 1.0F;

                            floatBuffer[i++] = color.r;
                            floatBuffer[i++] = color.g;
                            floatBuffer[i++] = color.b;
                            floatBuffer[i++] = 1.0F;
                        }
                    }
                }

            }
            env->ExceptionClear();
        }
        MobileRT::checkSystemError("rtInitColorsArray finish 1");
        return directBuffer;
    } catch (const ::std::bad_alloc &badAlloc) {
        handleException(env, badAlloc, "puscas/mobilertapp/exceptions/LowMemoryException");
    } catch (const ::std::exception &exception) {
        handleException(env, exception, "java/lang/RuntimeException");
    } catch (...) {
        handleException(env, ::std::exception{}, "java/lang/RuntimeException");
    }
    MobileRT::checkSystemError("rtInitColorsArray finish 2");
    return nullptr;
}

static void updateFps() {
    MobileRT::checkSystemError("updateFps start");
    static ::std::int32_t frame {};
    static ::std::chrono::steady_clock::time_point timebase {};
    ++frame;
    const auto timeNow {::std::chrono::steady_clock::now()};
    const auto timeElapsed {::std::chrono::duration_cast<::std::chrono::milliseconds>(timeNow - timebase).count()};
    fps_ = (static_cast<float>(frame) * 1000.0F) / static_cast<float>(timeElapsed);
    if (timeElapsed > 1000) {
        timebase = timeNow;
        frame = 0;
    }
    MobileRT::checkSystemError("updateFps finish");
}

extern "C"
void Java_puscas_mobilertapp_DrawView_rtStartRender(
    JNIEnv *env,
    jobject /*thiz*/,
    jboolean wait
) {
    try {
        MobileRT::checkSystemError("rtStartRender start");
        if (wait) {
            ::std::unique_lock<::std::mutex> lock {mutex_};
            rendered_.wait(lock, [&] { return finishedRendering_.load(); });
            finishedRendering_ = false;
        }
        state_ = State::BUSY;
        LOG_DEBUG("STATE = BUSY");
        env->ExceptionClear();
        MobileRT::checkSystemError("rtStartRender finish");
    } catch (const ::std::exception &exception) {
        handleException(env, exception, "java/lang/RuntimeException");
    }
}

extern "C"
void Java_puscas_mobilertapp_DrawView_rtStopRender(
    JNIEnv *env,
    jobject /*thiz*/,
    jboolean wait
) {
    MobileRT::checkSystemError("rtStopRender start");
    {
        LOG_DEBUG("Will get lock");
        state_ = State::STOPPED;
        LOG_DEBUG("STATE = STOPPED");
        ::std::unique_lock<::std::mutex> lock {mutex_};
        LOG_DEBUG("Got lock, waiting for renderer to finish");
        if (renderer_ != nullptr) {
            LOG_DEBUG("RENDERER STOP");
            renderer_->stopRender();
        }
        if (wait) {
            while (!finishedRendering_) {
                LOG_DEBUG("WILL TRY TO STOP RENDERER");
                if (renderer_ != nullptr) {
                    LOG_DEBUG("RENDERER STOP");
                    renderer_->stopRender();
                    break;
                }
                rendered_.wait_for(lock, ::std::chrono::seconds(3),
                                   [&] { return finishedRendering_.load(); });
            }
        }
        LOG_DEBUG("Renderer finished");
    }
    env->ExceptionClear();
    LOG_DEBUG("stopRender finished");
    MobileRT::checkSystemError("rtStopRender finish");
}

extern "C"
jint Java_puscas_mobilertapp_MainRenderer_rtInitialize(
    JNIEnv *env,
    jobject /*thiz*/,
    jobject localConfig
) {
    MobileRT::checkSystemError("rtInitialize start");
    LOG_DEBUG("INITIALIZE");
    try {
        const auto configClass{env->GetObjectClass(localConfig)};

        const auto sceneMethodId {env->GetMethodID(configClass, "getScene", "()I")};
        const auto sceneIndex {env->CallIntMethod(localConfig, sceneMethodId)};

        const auto shaderMethodId {env->GetMethodID(configClass, "getShader", "()I")};
        const auto shaderIndex {env->CallIntMethod(localConfig, shaderMethodId)};

        const auto acceleratorMethodId {env->GetMethodID(configClass, "getAccelerator", "()I")};
        const auto acceleratorIndex {env->CallIntMethod(localConfig, acceleratorMethodId)};


        const auto configResolutionMethodId {env->GetMethodID(configClass, "getConfigResolution",
                                                         "()Lpuscas/mobilertapp/configs/ConfigResolution;")};
        const auto resolutionConfig {env->CallObjectMethod(localConfig, configResolutionMethodId)};
        const auto resolutionConfigClass {env->GetObjectClass(resolutionConfig)};

        const auto widthMethodId {env->GetMethodID(resolutionConfigClass, "getWidth", "()I")};
        const auto width {env->CallIntMethod(resolutionConfig, widthMethodId)};

        const auto heightMethodId {env->GetMethodID(resolutionConfigClass, "getHeight", "()I")};
        const auto height {env->CallIntMethod(resolutionConfig, heightMethodId)};


        const auto configSamplesMethodId {env->GetMethodID(configClass, "getConfigSamples",
                                                      "()Lpuscas/mobilertapp/configs/ConfigSamples;")};
        const auto samplesConfig {env->CallObjectMethod(localConfig, configSamplesMethodId)};
        const auto samplesConfigClass {env->GetObjectClass(samplesConfig)};

        const auto samplesPixelMethodId {env->GetMethodID(samplesConfigClass, "getSamplesPixel", "()I")};
        const auto samplesPixel {env->CallIntMethod(samplesConfig, samplesPixelMethodId)};

        const auto samplesLightMethodId {env->GetMethodID(samplesConfigClass, "getSamplesLight", "()I")};
        const auto samplesLight {env->CallIntMethod(samplesConfig, samplesLightMethodId)};

        jboolean isCopy {JNI_FALSE};
        const auto objMethodId {env->GetMethodID(configClass, "getObjFilePath", "()Ljava/lang/String;")};
        const auto localObjFilePath {reinterpret_cast<jstring> (env->CallObjectMethod(localConfig, objMethodId))};
        const auto *const objFilePath {env->GetStringUTFChars(localObjFilePath, &isCopy)};

        const auto mtlMethodId {env->GetMethodID(configClass, "getMatFilePath", "()Ljava/lang/String;")};
        const auto localMatFilePath {reinterpret_cast<jstring> (env->CallObjectMethod(localConfig, mtlMethodId))};
        const auto *const matFilePath {env->GetStringUTFChars(localMatFilePath, &isCopy)};

        const auto camMethodId {env->GetMethodID(configClass, "getCamFilePath", "()Ljava/lang/String;")};
        const auto localCamFilePath {reinterpret_cast<jstring> (env->CallObjectMethod(localConfig, camMethodId))};
        const auto *const camFilePath {env->GetStringUTFChars(localCamFilePath, &isCopy)};

        const auto res {
            [&]() -> ::std::int32_t {
                const ::std::lock_guard<::std::mutex> lock {mutex_};
                renderer_ = nullptr;
                const auto ratio{static_cast<float> (width) / height};
                ::MobileRT::Scene scene{};
                ::std::unique_ptr<::MobileRT::Sampler> samplerPixel{};
                ::std::unique_ptr<::MobileRT::Shader> shader{};
                ::std::unique_ptr<::MobileRT::Camera> camera{};
                ::glm::vec3 maxDist{};
                LOG_DEBUG("LOADING SCENE: ", sceneIndex);
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
                        camera = cornellBox_Cam(ratio);
                        maxDist = ::glm::vec3{1, 1, 1};
                        break;

                    case 3:
                        scene = spheres2_Scene(::std::move(scene));
                        camera = spheres2_Cam(ratio);
                        maxDist = ::glm::vec3{8, 8, 8};
                        break;

                    default: {
                        ASSERT(objFilePath != nullptr, "OBJ file path not valid.");
                        ASSERT(matFilePath != nullptr, "MTL file path not valid.");
                        ASSERT(camFilePath != nullptr, "CAM file path not valid.");

                        const auto cameraFactory{::Components::CameraFactory()};
                        camera = cameraFactory.loadFromFile(camFilePath, ratio);

                        ::Components::OBJLoader objLoader {objFilePath, matFilePath};
                        MobileRT::checkSystemError("rtInitialize after loading OBJ");
                        LOG_DEBUG("OBJLOADER PROCESSED");

                        if (!objLoader.isProcessed()) {
                            return -1;
                        }
                        const auto sceneBuilt {objLoader.fillScene(&scene,
                            []() {return ::MobileRT::std::make_unique<Components::StaticPCG>();}
                        )};
                        MobileRT::checkSystemError("rtInitialize after filling scene");
                        if (!sceneBuilt) {
                            return -1;
                        }

                        maxDist = ::glm::vec3{1, 1, 1};
                    }
                        break;
                }
                samplerPixel = samplesPixel <= 1
                   ? ::std::unique_ptr<::MobileRT::Sampler>(::MobileRT::std::make_unique<Components::Constant>(0.5F))
                   : ::std::unique_ptr<::MobileRT::Sampler>(::MobileRT::std::make_unique<Components::StaticPCG>());
                LOG_DEBUG("LOADING SHADER: ", shaderIndex);
                LOG_DEBUG("LOADING ACCELERATOR: ", ::MobileRT::Shader::Accelerator(acceleratorIndex));
                LOG_DEBUG("samplesLight: ", samplesLight);
                MobileRT::checkSystemError("rtInitialize before loading shader");
                const auto start {::std::chrono::system_clock::now()};
                switch (shaderIndex) {
                    case 1: {
                        shader = ::MobileRT::std::make_unique<Components::Whitted>(
                            ::std::move(scene),
                            samplesLight,
                            ::MobileRT::Shader::Accelerator(acceleratorIndex)
                        );
                        break;
                    }

                    case 2: {
                        ::std::unique_ptr<MobileRT::Sampler> samplerRussianRoulette{
                            ::MobileRT::std::make_unique<Components::StaticPCG>()
                        };

                        shader = ::MobileRT::std::make_unique<Components::PathTracer>(
                            ::std::move(scene),
                            ::std::move(samplerRussianRoulette),
                            samplesLight,
                            ::MobileRT::Shader::Accelerator(acceleratorIndex)
                        );
                        break;
                    }

                    case 3: {
                        shader = ::MobileRT::std::make_unique<Components::DepthMap>(
                            ::std::move(scene), maxDist,
                            ::MobileRT::Shader::Accelerator(acceleratorIndex)
                        );
                        break;
                    }

                    case 4: {
                        shader = ::MobileRT::std::make_unique<Components::DiffuseMaterial>(
                            ::std::move(scene), ::MobileRT::Shader::Accelerator(acceleratorIndex)
                        );
                        break;
                    }

                    default: {
                        shader = ::MobileRT::std::make_unique<Components::NoShadows>(
                            ::std::move(scene),
                            samplesLight,
                            ::MobileRT::Shader::Accelerator(acceleratorIndex)
                        );
                        break;
                    }
                }
                const auto end {::std::chrono::system_clock::now()};
                MobileRT::checkSystemError("rtInitialize after loading shader");

                LOG_DEBUG("LOADING RENDERER");
                const auto planes {static_cast<::std::int32_t> (shader->getPlanes().size())};
                const auto spheres {static_cast<::std::int32_t> (shader->getSpheres().size())};
                const auto triangles {static_cast<::std::int32_t> (shader->getTriangles().size())};
                const auto materials {static_cast<::std::int32_t> (shader->getMaterials().size())};
                numLights_ = static_cast<::std::int32_t> (shader->getLights().size());
                const auto nPrimitives {triangles + spheres + planes};
                renderer_ = ::MobileRT::std::make_unique<::MobileRT::Renderer>(
                    ::std::move(shader), ::std::move(camera), ::std::move(samplerPixel),
                    width, height, samplesPixel
                );
                timeRenderer_ = ::std::chrono::duration_cast<::std::chrono::milliseconds>(end - start).count();
                LOG_INFO("TIME CONSTRUCTION RENDERER = ", timeRenderer_, "ms");
                LOG_DEBUG("PLANES = ", planes);
                LOG_DEBUG("SPHERES = ", spheres);
                LOG_DEBUG("TRIANGLES = ", triangles);
                LOG_DEBUG("LIGHTS = ", numLights_);
                LOG_DEBUG("MATERIALS = ", materials);
                LOG_DEBUG("TOTAL PRIMITIVES = ", nPrimitives);
                MobileRT::checkSystemError("rtInitialize almost finished");
                return nPrimitives;
            }()};

        env->ExceptionClear();
        LOG_DEBUG("PRIMITIVES = ", res);
        MobileRT::checkSystemError("rtInitialize finish");
        return res;
    } catch (const ::std::bad_alloc &badAlloc) {
        handleException(env, badAlloc, "puscas/mobilertapp/exceptions/LowMemoryException");
        return -1;
    } catch (const ::std::exception &exception) {
        handleException(env, exception, "java/lang/RuntimeException");
        return -2;
    } catch (...) {
        handleException(env, ::std::exception{}, "java/lang/RuntimeException");
        return -3;
    }
}

extern "C"
void Java_puscas_mobilertapp_MainRenderer_rtFinishRender(
    JNIEnv *env,
    jobject /*thiz*/
) {
    MobileRT::checkSystemError("rtFinishRender start");

    {
        const ::std::lock_guard<::std::mutex> lock {mutex_};
        state_ = State::FINISHED;
        LOG_DEBUG("STATE = FINISHED");
        if (renderer_ != nullptr) {
            renderer_->stopRender();
        }
        if (thread_ != nullptr) {
            thread_ = nullptr;
            LOG_DEBUG("DELETED RENDERER");
        }
        state_ = State::IDLE;
        LOG_DEBUG("STATE = IDLE");
        fps_ = 0.0F;
        timeRenderer_ = 0;
        finishedRendering_ = true;
        env->ExceptionClear();
    }
    MobileRT::checkSystemError("rtFinishRender finish");
}

extern "C"
void Java_puscas_mobilertapp_MainRenderer_rtRenderIntoBitmap(
    JNIEnv *env,
    jobject /*thiz*/,
    jobject localBitmap,
    jint nThreads
) {
    MobileRT::checkSystemError("rtRenderIntoBitmap start");
    LOG_DEBUG("rtRenderIntoBitmap");
    LOG_DEBUG("nThreads = ", nThreads);
    try {
        auto globalBitmap {static_cast<jobject> (env->NewGlobalRef(localBitmap))};

        auto lambda {
            [=]() -> void {
                ::std::chrono::duration<double> timeRendering {};

                LOG_DEBUG("rtRenderIntoBitmap step 1");
                ASSERT(env != nullptr, "JNIEnv not valid.");
                const auto jniError {
                    javaVM_->GetEnv(reinterpret_cast<void **> (const_cast<JNIEnv **> (&env)),
                                    JNI_VERSION_1_6)
                };

                LOG_DEBUG("rtRenderIntoBitmap step 2");
                ASSERT(jniError == JNI_OK || jniError == JNI_EDETACHED, "JNIEnv not valid.");
                {
                    const auto result {javaVM_->AttachCurrentThread(const_cast<JNIEnv **> (&env), nullptr)};
                    ASSERT(result == JNI_OK, "Couldn't attach current thread to JVM.");
                    static_cast<void> (result);
                }

                LOG_DEBUG("rtRenderIntoBitmap step 3");
                ::std::int32_t *dstPixels{};
                {
                    const auto ret{
                        AndroidBitmap_lockPixels(env, globalBitmap,
                                                 reinterpret_cast<void **> (&dstPixels))
                    };
                    ASSERT(ret == JNI_OK, "Couldn't lock the Android bitmap pixels.");
                    LOG_DEBUG("ret = ", ret);
                }

                LOG_DEBUG("rtRenderIntoBitmap step 4");
                AndroidBitmapInfo info{};
                {
                    const auto ret{AndroidBitmap_getInfo(env, globalBitmap, &info)};
                    ASSERT(ret == JNI_OK, "Couldn't get the Android bitmap information structure.");
                    LOG_DEBUG("ret = ", ret);
                }

                LOG_DEBUG("rtRenderIntoBitmap step 5");
                ::std::int32_t rep{1};
                LOG_DEBUG("WILL START TO RENDER");
                const auto startRendering {::std::chrono::system_clock::now()};
                while (state_ == State::BUSY && rep > 0) {
                    LOG_DEBUG("STARTING RENDERING");
                    LOG_DEBUG("nThreads = ", nThreads);
                    {
                        if (renderer_ != nullptr) {
                            renderer_->renderFrame(dstPixels, nThreads);
                        }
                    }
                    LOG_DEBUG("FINISHED RENDERING");
                    updateFps();
                    rep--;
                }
                const auto endRendering {::std::chrono::system_clock::now()};
                timeRendering = endRendering - startRendering;
                LOG_DEBUG("RENDER FINISHED");
                finishedRendering_ = true;
                rendered_.notify_all();
                {
                    const ::std::lock_guard<::std::mutex> lock {mutex_};
                    if (state_ != State::STOPPED) {
                        state_ = State::FINISHED;
                        LOG_DEBUG("STATE = FINISHED");
                    }
                    {
                        const auto result{AndroidBitmap_unlockPixels(env, globalBitmap)};
                        ASSERT(result == JNI_OK, "Couldn't unlock the Android bitmap pixels.");
                        static_cast<void> (result);
                    }

                    env->DeleteGlobalRef(globalBitmap);
                    {
                        const auto result {
                            javaVM_->GetEnv(reinterpret_cast<void **> (const_cast<JNIEnv **> (&env)), JNI_VERSION_1_6)
                        };
                        ASSERT(result == JNI_OK || jniError == JNI_EDETACHED, "JNIEnv not valid.");
                        static_cast<void> (result);
                    }
                    env->ExceptionClear();
                    if (jniError == JNI_EDETACHED) {
                        const auto result {javaVM_->DetachCurrentThread()};
                        ASSERT(result == JNI_OK, "Couldn't detach the current thread from JVM.");
                        static_cast<void> (result);
                    }
                }
                const auto renderingTime {timeRendering.count()};
                const auto castedRays {renderer_->getTotalCastedRays()};
                LOG_DEBUG("Rendering Time in secs = ", renderingTime);
                LOG_DEBUG("Casted rays = ", castedRays);
                LOG_DEBUG("Total Millions rays per second = ", (static_cast<double> (castedRays) / renderingTime)
                    / 1000000L);

                LOG_DEBUG("rtRenderIntoBitmap finished");
            }
        };

        thread_ = ::MobileRT::std::make_unique<::std::thread>(lambda);
        thread_->detach();

        LOG_DEBUG("rtRenderIntoBitmap finished preparing");
        MobileRT::checkSystemError("rtRenderIntoBitmap finish");
        env->ExceptionClear();
    } catch (const ::std::bad_alloc &badAlloc) {
        handleException(env, badAlloc, "puscas/mobilertapp/exceptions/LowMemoryException");
    } catch (const ::std::exception &exception) {
        handleException(env, exception, "java/lang/RuntimeException");
    } catch (...) {
        handleException(env, ::std::exception{}, "java/lang/RuntimeException");
    }
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_RenderTask_rtGetState(
    JNIEnv *env,
    jobject /*thiz*/
) {
    MobileRT::checkSystemError("rtGetState start");

    const auto res{static_cast<::std::int32_t> (state_.load())};
    env->ExceptionClear();
    MobileRT::checkSystemError("rtGetState finish");
    return res;
}

extern "C"
float Java_puscas_mobilertapp_RenderTask_rtGetFps(
    JNIEnv *env,
    jobject /*thiz*/
) {
    if (errno == ETIMEDOUT || errno == EBADF) {
        // Ignore connection timed out
        // Ignore bad file descriptor (necessary for Android API 24)
        errno = 0;
    }
    MobileRT::checkSystemError("rtGetFps start");
    env->ExceptionClear();
    MobileRT::checkSystemError("rtGetFps finish");
    return fps_;
}

extern "C"
jlong Java_puscas_mobilertapp_RenderTask_rtGetTimeRenderer(
    JNIEnv *env,
    jobject /*thiz*/
) {
    MobileRT::checkSystemError("rtGetTimeRenderer start");
    env->ExceptionClear();
    MobileRT::checkSystemError("rtGetTimeRenderer finish");
    return timeRenderer_;
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_RenderTask_rtGetSample(
    JNIEnv *env,
    jobject /*thiz*/
) {
    if (errno == EBADF) {
        // Ignore bad file descriptor (necessary for Android API 24)
        errno = 0;
    }
    MobileRT::checkSystemError("rtGetSample start");
    ::std::int32_t sample{};
    {
        const ::std::lock_guard<::std::mutex> lock {mutex_};
        if (renderer_ != nullptr) {
            sample = renderer_->getSample();
        }
    }
    env->ExceptionClear();
    MobileRT::checkSystemError("rtGetSample finish");
    return sample;
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_MainActivity_rtResize(
    JNIEnv *env,
    jobject /*thiz*/,
    jint size
) {
    MobileRT::checkSystemError("rtResize start");

    const auto res{
        ::MobileRT::roundDownToMultipleOf(
            size, static_cast<::std::int32_t> (::std::sqrt(::MobileRT::NumberOfTiles))
        )
    };
    env->ExceptionClear();
    MobileRT::checkSystemError("rtResize finish");
    return res;
}

extern "C"
void Java_puscas_mobilertapp_MainActivity_resetErrno(
    JNIEnv *env,
    jclass /*thiz*/
) {
    errno = 0;
    MobileRT::checkSystemError("resetErrno start");
    env->ExceptionClear();
    MobileRT::checkSystemError("resetErrno finish");
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_DrawView_rtGetNumberOfLights(
    JNIEnv *env,
    jobject /*thiz*/
) {
    MobileRT::checkSystemError("rtGetNumberOfLights start");
    env->ExceptionClear();
    MobileRT::checkSystemError("rtGetNumberOfLights finish");
    return numLights_;
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_rtFreeNativeBuffer(
    JNIEnv *env,
    jobject /*thiz*/,
    jobject bufferRef
) {
    MobileRT::checkSystemError("rtFreeNativeBuffer start");
    if (bufferRef != nullptr) {
        auto *buffer{env->GetDirectBufferAddress(bufferRef)};
        float *const floatBuffer{static_cast<float *> (buffer)};
        delete[] floatBuffer;
    }
    MobileRT::checkSystemError("rtFreeNativeBuffer finish");
    return nullptr;
}
