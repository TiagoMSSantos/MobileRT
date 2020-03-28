#include "DrawView.hpp"

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

#include <android/bitmap.h>
#include <glm/glm.hpp>
#include <fstream>
#include <mutex>
#include <string>
#include <boost/assert.hpp>

static float fps_ {};
static ::std::atomic<::State> state_ {State::IDLE};
static ::std::unique_ptr<::MobileRT::Renderer> renderer_ {};
static ::std::unique_ptr<::JavaVM> javaVM_ {};
static ::std::unique_ptr<::std::thread> thread_ {};
static ::std::mutex mutex_ {};
static ::std::int32_t numLights_ {};
static ::std::int64_t timeRenderer_ {};
static ::std::condition_variable rendered_ {};
static ::std::atomic<bool> finishedRendering_ {true};

extern "C"
::std::int32_t JNI_OnLoad(JavaVM *const jvm, void * /*reserved*/) {
    LOG("JNI_OnLoad");
    javaVM_.reset(jvm);

    JNIEnv *jniEnv {};
    {
        const ::std::int32_t result {javaVM_->GetEnv(reinterpret_cast<void**> (&jniEnv), JNI_VERSION_1_6)};
        BOOST_ASSERT_MSG(result == JNI_OK, "JNI was not loaded properly.");
        static_cast<void> (result);
    }
    BOOST_ASSERT_MSG(jniEnv != nullptr, "JNIEnv was not loaded properly.");
    jniEnv->ExceptionClear();
    return JNI_VERSION_1_6;
}

extern "C"
void JNI_OnUnload(JavaVM *const /*jvm*/, void * /*reserved*/) {
    LOG("JNI_OnUnload");
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_rtInitCameraArray(
        JNIEnv *env,
        jobject /*thiz*/
) {
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
                        ::std::int32_t i {};

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

                        const auto *const perspective {dynamic_cast<::Components::Perspective*> (camera)};
                        const auto *const orthographic {dynamic_cast<::Components::Orthographic*> (camera)};
                        if (perspective != nullptr) {
                            const float hFov {perspective->getHFov()};
                            const float vFov {perspective->getVFov()};
                            floatBuffer[i++] = hFov;
                            floatBuffer[i++] = vFov;
                            floatBuffer[i++] = 0.0F;
                            floatBuffer[i++] = 0.0F;
                        }
                        if (orthographic != nullptr) {
                            const float sizeH {orthographic->getSizeH()};
                            const float sizeV {orthographic->getSizeV()};
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
        return directBuffer;
    } catch (const ::std::bad_alloc &badAlloc) {
        const auto lowMemClass {env->FindClass("puscas/mobilertapp/exceptions/LowMemoryException")};
        const auto res {env->ThrowNew(lowMemClass, badAlloc.what())};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("LowMemoryException thrown");
        }
        return nullptr;
    } catch (const ::std::exception &exception) {
        const auto exceptionClass {env->FindClass("java/lang/RuntimeException")};
        const auto res { env->ThrowNew(exceptionClass, exception.what())};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("RuntimeException thrown");
        }
        return nullptr;
    } catch (...) {
        const auto exceptionClass {env->FindClass("java/lang/RuntimeException")};
        const auto res {env->ThrowNew(exceptionClass, "Unknown error")};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("RuntimeException thrown");
        }
        return nullptr;
    }
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_rtInitVerticesArray(
        JNIEnv *env,
        jobject /*thiz*/
) {
    try {
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
        return directBuffer;
    } catch (const ::std::bad_alloc &badAlloc) {
        const auto lowMemClass {env->FindClass("puscas/mobilertapp/exceptions/LowMemoryException")};
        const auto res {env->ThrowNew(lowMemClass, badAlloc.what())};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("LowMemoryException thrown");
        }
    } catch (const ::std::exception &exception) {
        const auto exceptionClass {env->FindClass("java/lang/RuntimeException")};
        const auto res {env->ThrowNew(exceptionClass, exception.what())};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("RuntimeException thrown");
        }
    } catch (...) {
        const auto exceptionClass {env->FindClass("java/lang/RuntimeException")};
        const auto res {env->ThrowNew(exceptionClass, "Unknown error")};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("RuntimeException thrown");
        }
    }
    return nullptr;
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_rtInitColorsArray(
        JNIEnv *env,
        jobject /*thiz*/
) {
    try {
        jobject directBuffer {};
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
                            const auto materialIndex {triangle.getMaterialIndex()};
                            auto material {::MobileRT::Material {}};
                            if (materialIndex >= 0) {
                                material = renderer_->shader_->getMaterials()
                                    [static_cast<::std::uint32_t> (materialIndex)];
                            }

                            const auto &kD {material.Kd_};
                            const auto &kS {material.Ks_};
                            const auto &kT {material.Kt_};
                            const auto &lE {material.Le_};
                            auto color {kD};

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
        return directBuffer;
    } catch (const ::std::bad_alloc &badAlloc) {
        const auto lowMemClass {env->FindClass("puscas/mobilertapp/exceptions/LowMemoryException")};
        const auto res {env->ThrowNew(lowMemClass, badAlloc.what())};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("LowMemoryException thrown");
        }
    } catch (const ::std::exception &exception) {
        const auto exceptionClass {env->FindClass("java/lang/RuntimeException")};
        const auto res {env->ThrowNew(exceptionClass, exception.what())};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("RuntimeException thrown");
        }
    } catch (...) {
        const auto exceptionClass {env->FindClass("java/lang/RuntimeException")};
        const auto res {env->ThrowNew(exceptionClass, "Unknown error")};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("RuntimeException thrown");
        }
    }
    return nullptr;
}

static void updateFps() {
    static ::std::int32_t frame {};
    static ::std::chrono::steady_clock::time_point timebase {};
    ++frame;
    const auto timeNow {::std::chrono::steady_clock::now()};
    const auto timeElapsed {::std::chrono::duration_cast<std::chrono::milliseconds> (timeNow - timebase).count()};
    if (timeElapsed > 1000) {
        fps_ = (frame * 1000.0F) / timeElapsed;
        timebase = timeNow;
        frame = 0;
    }
}

extern "C"
void Java_puscas_mobilertapp_DrawView_rtStartRender(
        JNIEnv *env,
        jobject /*thiz*/
) {
    ::std::unique_lock<std::mutex> lock {mutex_};
    rendered_.wait(lock, [&]{return finishedRendering_ == true;});
    finishedRendering_ = false;
    state_ = State::BUSY;
    LOG("STATE = BUSY");
    env->ExceptionClear();
}

extern "C"
void Java_puscas_mobilertapp_DrawView_rtStopRender(
        JNIEnv *env,
        jobject /*thiz*/
) {
    {
        LOG("Will get lock");
        state_ = State::STOPPED;
        LOG("STATE = STOPPED");
        ::std::unique_lock<std::mutex> lock {mutex_};
        LOG("Got lock, waiting for renderer to finish");
//        while (!finishedRendering_) {
            if (renderer_ != nullptr) {
                LOG("RENDERER STOP");
                renderer_->stopRender();
            }
//            rendered_.wait(lock, [&]{return finishedRendering_ == true;});
//        }
        LOG("Renderer finished");
    }
    env->ExceptionClear();
    LOG("stopRender finished");
}

extern "C"
jint Java_puscas_mobilertapp_MainRenderer_rtInitialize(
        JNIEnv *env,
        jobject /*thiz*/,
        jobject localConfig
) {
    LOG("INITIALIZE");
    try {
        const auto configClass {env->GetObjectClass(localConfig)};

        const auto sceneMethodId {env->GetMethodID(configClass, "getScene", "()I")};
        const auto sceneIndex {env->CallIntMethod(localConfig, sceneMethodId)};

        const auto shaderMethodId {env->GetMethodID(configClass, "getShader", "()I")};
        const auto shaderIndex {env->CallIntMethod(localConfig, shaderMethodId)};

        const auto acceleratorMethodId {env->GetMethodID(configClass, "getAccelerator", "()I")};
        const auto acceleratorIndex {env->CallIntMethod(localConfig, acceleratorMethodId)};

        const auto widthMethodId {env->GetMethodID(configClass, "getWidth", "()I")};
        const auto width {env->CallIntMethod(localConfig, widthMethodId)};

        const auto heightMethodId {env->GetMethodID(configClass, "getHeight", "()I")};
        const auto height {env->CallIntMethod(localConfig, heightMethodId)};

        const auto samplesPixelMethodId {env->GetMethodID(configClass, "getSamplesPixel", "()I")};
        const auto samplesPixel {env->CallIntMethod(localConfig, samplesPixelMethodId)};

        const auto samplesLightMethodId {env->GetMethodID(configClass, "getSamplesLight", "()I")};
        const auto samplesLight {env->CallIntMethod(localConfig, samplesLightMethodId)};

        jboolean isCopy {JNI_FALSE};
        const auto objMethodId {env->GetMethodID(configClass, "getObjFilePath", "()Ljava/lang/String;")};
        const auto localObjFilePath {static_cast<jstring> (env->CallObjectMethod(localConfig, objMethodId))};
        const auto *const objFilePath {env->GetStringUTFChars(localObjFilePath, &isCopy)};

        const auto mtlMethodId {env->GetMethodID(configClass, "getMatFilePath", "()Ljava/lang/String;")};
        const auto localMatFilePath {static_cast<jstring> (env->CallObjectMethod(localConfig, mtlMethodId))};
        const auto *const matFilePath {env->GetStringUTFChars(localMatFilePath, &isCopy)};

        const auto camMethodId {env->GetMethodID(configClass, "getCamFilePath", "()Ljava/lang/String;")};
        const auto localCamFilePath {static_cast<jstring> (env->CallObjectMethod(localConfig, camMethodId))};
        const auto *const camFilePath {env->GetStringUTFChars(localCamFilePath, &isCopy)};

        const auto res {
            [&]() -> ::std::int32_t {
                const ::std::lock_guard<::std::mutex> lock {mutex_};
                renderer_ = nullptr;
                const auto ratio {static_cast<float> (width) / height};
                ::MobileRT::Scene scene {};
                ::std::unique_ptr<::MobileRT::Sampler> samplerPixel {};
                ::std::unique_ptr<::MobileRT::Shader> shader {};
                ::std::unique_ptr<::MobileRT::Camera> camera {};
                ::glm::vec3 maxDist {};
                LOG("LOADING SCENE");
                switch (sceneIndex) {
                    case 0: {
                        const auto fovX {45.0F * ratio};
                        const auto fovY {45.0F};
                        camera = ::std::make_unique<Components::Perspective> (
                            ::glm::vec3 {0.0F, 0.0F, -3.4F},
                            ::glm::vec3 {0.0F, 0.0F, 1.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY
                        );
                        scene = cornellBoxScene(::std::move(scene));
                        maxDist = ::glm::vec3{1, 1, 1};
                    }
                        break;

                    case 1: {
                        const auto sizeH {10.0F * ratio};
                        const auto sizeV {10.0F};
                        camera = ::std::make_unique<Components::Orthographic> (
                            ::glm::vec3 {0.0F, 1.0F, -10.0F},
                            ::glm::vec3 {0.0F, 1.0F, 7.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            sizeH, sizeV
                        );
                        scene = spheresScene(::std::move(scene));
                        maxDist = ::glm::vec3{8, 8, 8};
                    }
                        break;

                    case 2: {
                        const auto fovX {45.0F * ratio};
                        const auto fovY {45.0F};
                        camera = ::std::make_unique<Components::Perspective> (
                            ::glm::vec3{0.0F, 0.0F, -3.4F},
                            ::glm::vec3{0.0F, 0.0F, 1.0F},
                            ::glm::vec3{0.0F, 1.0F, 0.0F},
                            fovX, fovY
                        );
                        scene = cornellBoxScene2(::std::move(scene));
                        maxDist = ::glm::vec3{1, 1, 1};
                    }
                        break;

                    case 3: {
                        const auto fovX {60.0F * ratio};
                        const auto fovY {60.0F};
                        camera = ::std::make_unique<Components::Perspective> (
                            ::glm::vec3{0.0F, 0.5F, 1.0F},
                            ::glm::vec3{0.0F, 0.0F, 7.0F},
                            ::glm::vec3{0.0F, 1.0F, 0.0F},
                            fovX, fovY
                        );
                        scene = spheresScene2(::std::move(scene));
                        maxDist = ::glm::vec3 {8, 8, 8};
                    }
                        break;

                    default: {
                        BOOST_ASSERT_MSG(objFilePath != nullptr, "OBJ file path not valid.");
                        BOOST_ASSERT_MSG(matFilePath != nullptr, "MTL file path not valid.");
                        BOOST_ASSERT_MSG(camFilePath != nullptr, "CAM file path not valid.");

                        const auto cameraFactory {::Components::CameraFactory()};
                        camera = cameraFactory.loadFromFile(camFilePath, ratio);

                        ::Components::OBJLoader objLoader {objFilePath, matFilePath};
                        LOG("OBJLOADER PROCESSED");

                        if (!objLoader.isProcessed()) {
                            return -1;
                        }
                        const auto sceneBuilt {objLoader.fillScene(
                                &scene, []() {return ::std::make_unique<Components::StaticHaltonSeq> ();}
                        )};
                        if (!sceneBuilt) {
                            return -1;
                        }

                        maxDist = ::glm::vec3 {1, 1, 1};
                    }
                        break;
                }
                samplerPixel = samplesPixel <= 1
                    ? ::std::unique_ptr<::MobileRT::Sampler> (::std::make_unique<Components::Constant> (0.5F))
                    : ::std::unique_ptr<::MobileRT::Sampler> (::std::make_unique<Components::StaticHaltonSeq> ());
                LOG("LOADING SHADER");
                const auto start {::std::chrono::system_clock::now()};
                switch (shaderIndex) {
                    case 1: {
                        shader = ::std::make_unique<Components::Whitted> (
                            ::std::move(scene),
                            samplesLight,
                            ::MobileRT::Shader::Accelerator(acceleratorIndex)
                        );
                        break;
                    }

                    case 2: {
                        ::std::unique_ptr<MobileRT::Sampler> samplerRussianRoulette {
                            ::std::make_unique<Components::StaticHaltonSeq> ()
                        };

                        shader = ::std::make_unique<Components::PathTracer> (
                            ::std::move(scene),
                            ::std::move(samplerRussianRoulette),
                            samplesLight,
                            ::MobileRT::Shader::Accelerator(acceleratorIndex)
                        );
                        break;
                    }

                    case 3: {
                        shader = ::std::make_unique<Components::DepthMap> (
                            ::std::move(scene), maxDist, ::MobileRT::Shader::Accelerator(acceleratorIndex)
                        );
                        break;
                    }

                    case 4: {
                        shader = ::std::make_unique<Components::DiffuseMaterial> (
                            ::std::move(scene), ::MobileRT::Shader::Accelerator(acceleratorIndex)
                        );
                        break;
                    }

                    default: {
                        shader = ::std::make_unique<Components::NoShadows> (
                            ::std::move(scene),
                            samplesLight,
                            ::MobileRT::Shader::Accelerator(acceleratorIndex)
                        );
                        break;
                    }
                }
                const auto end {::std::chrono::system_clock::now()};

                LOG("LOADING RENDERER");
                const auto planes {static_cast<::std::int32_t> (shader->getPlanes().size())};
                const auto spheres {static_cast<::std::int32_t> (shader->getSpheres().size())};
                const auto triangles {static_cast<::std::int32_t> (shader->getTriangles().size())};
                const auto materials {static_cast<::std::int32_t> (shader->getMaterials().size())};
                numLights_ = static_cast<::std::int32_t> (shader->getLights().size());
                const auto nPrimitives {triangles + spheres + planes};
                renderer_ = ::std::make_unique<::MobileRT::Renderer> (
                    ::std::move(shader), ::std::move(camera), ::std::move(samplerPixel),
                    width, height, samplesPixel
                );
                timeRenderer_ = ::std::chrono::duration_cast<std::chrono::milliseconds> (end - start).count();
                LOG("TIME CONSTRUCTION RENDERER = ", timeRenderer_, "ms");
                LOG("PLANES = ", planes);
                LOG("SPHERES = ", spheres);
                LOG("TRIANGLES = ", triangles);
                LOG("LIGHTS = ", numLights_);
                LOG("MATERIALS = ", materials);
                return nPrimitives;
            }()};

        env->ExceptionClear();
        LOG("PRIMITIVES = ", res);
        return res;
    } catch (const ::std::bad_alloc &badAlloc) {
        const auto lowMemClass {env->FindClass("puscas/mobilertapp/exceptions/LowMemoryException")};
        const auto res {env->ThrowNew(lowMemClass, badAlloc.what())};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("LowMemoryException thrown");
        }
        return -1;
    } catch (const ::std::exception &exception) {
        const auto exceptionClass {env->FindClass("java/lang/RuntimeException")};
        const auto res {env->ThrowNew(exceptionClass, exception.what())};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("RuntimeException thrown");
        }
        return -2;
    } catch (...) {
        const auto exceptionClass {env->FindClass("java/lang/RuntimeException")};
        const auto res {env->ThrowNew(exceptionClass, "Unknown error")};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("RuntimeException thrown");
        }
        return -3;
    }
}

extern "C"
void Java_puscas_mobilertapp_MainRenderer_rtFinishRender(
        JNIEnv *env,
        jobject /*thiz*/
) {
    {
        const ::std::lock_guard<::std::mutex> lock {mutex_};
        state_ = State::FINISHED;
        LOG("STATE = FINISHED");
        if (renderer_ != nullptr) {
            renderer_->stopRender();
        }
        if (thread_ != nullptr) {
            thread_ = nullptr;
            LOG("DELETED RENDERER");
        }
        state_ = State::IDLE;
        LOG("STATE = IDLE");
        fps_ = 0.0F;
        timeRenderer_ = 0;
        finishedRendering_ = true;
        env->ExceptionClear();
    }
}

extern "C"
void Java_puscas_mobilertapp_MainRenderer_rtRenderIntoBitmap(
        JNIEnv *env,
        jobject /*thiz*/,
        jobject localBitmap,
        jint nThreads,
        jboolean async
) {
    try {
        auto globalBitmap {static_cast<jobject> (env->NewGlobalRef(localBitmap))};

        auto lambda {
            [=]() -> void {
                BOOST_ASSERT_MSG(env != nullptr, "JNIEnv not valid.");
                const auto jniError {
                        javaVM_->GetEnv(reinterpret_cast<void **> (const_cast<JNIEnv **> (&env)), JNI_VERSION_1_6)
                };

                BOOST_ASSERT_MSG(jniError == JNI_OK || jniError == JNI_EDETACHED, "JNIEnv not valid.");
                {
                    const auto result {javaVM_->AttachCurrentThread(const_cast<JNIEnv **> (&env), nullptr)};
                    BOOST_ASSERT_MSG(result == JNI_OK, "Couldn't attach current thread to JVM.");
                    static_cast<void> (result);
                }

                ::std::int32_t *dstPixels {};
                {
                    const auto ret {
                            AndroidBitmap_lockPixels(env, globalBitmap, reinterpret_cast<void **> (&dstPixels))
                    };
                    BOOST_ASSERT_MSG(ret == JNI_OK, "Couldn't lock the Android bitmap pixels.");
                    LOG("ret = ", ret);
                }

                AndroidBitmapInfo info {};
                {
                    const auto ret {AndroidBitmap_getInfo(env, globalBitmap, &info)};
                    BOOST_ASSERT_MSG(ret == JNI_OK, "Couldn't get the Android bitmap information structure.");
                    LOG("ret = ", ret);
                }

                ::std::int32_t rep {1};
                while (state_ == State::BUSY && rep > 0) {
                    LOG("STARTING RENDERING");
                    LOG("nThreads = ", nThreads);
                    {
//                        const ::std::lock_guard<::std::mutex> lock {mutex_};
                        if (renderer_ != nullptr) {
                            renderer_->renderFrame(dstPixels, nThreads);
                        }
                    }
                    LOG("FINISHED RENDERING");
                    updateFps();
                    rep--;
                }
                finishedRendering_ = true;
                rendered_.notify_all();
                {
                    const ::std::lock_guard<::std::mutex> lock {mutex_};
                    if (state_ != State::STOPPED) {
                        state_ = State::FINISHED;
                        LOG("STATE = FINISHED");
                    }
                    {
                        const auto result {AndroidBitmap_unlockPixels(env, globalBitmap)};
                        BOOST_ASSERT_MSG(result == JNI_OK, "Couldn't unlock the Android bitmap pixels.");
                        static_cast<void> (result);
                    }

                    env->DeleteGlobalRef(globalBitmap);
                    {
                        const auto result {
                                javaVM_->GetEnv(reinterpret_cast<void **> (const_cast<JNIEnv **> (&env)),
                                                JNI_VERSION_1_6)
                        };
                        BOOST_ASSERT_MSG(result == JNI_OK || jniError == JNI_EDETACHED, "JNIEnv not valid.");
                        static_cast<void> (result);
                    }
                    env->ExceptionClear();
                    if (jniError == JNI_EDETACHED) {
                        const auto result {javaVM_->DetachCurrentThread()};
                        BOOST_ASSERT_MSG(result == JNI_OK, "Couldn't detach the current thread from JVM.");
                        static_cast<void> (result);
                    }
                }
            }
        };

        if (async) {
            thread_ = ::std::make_unique<::std::thread>(lambda);
            thread_->detach();
        } else {
            lambda();
        }
        env->ExceptionClear();
    } catch (const ::std::bad_alloc &badAlloc) {
        const auto lowMemClass {env->FindClass("puscas/mobilertapp/exceptions/LowMemoryException")};
        const auto res {env->ThrowNew(lowMemClass, badAlloc.what())};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("LowMemoryException thrown");
        }
    } catch (const ::std::exception &exception) {
        const auto exceptionClass {env->FindClass("java/lang/RuntimeException")};
        const auto res {env->ThrowNew(exceptionClass, exception.what())};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("RuntimeException thrown");
        }
    } catch (...) {
        const auto exceptionClass {env->FindClass("java/lang/RuntimeException")};
        const auto res {env->ThrowNew(exceptionClass, "Unknown error")};
        if (res != 0) {
            LOG("ERROR: ", res);
        } else {
            LOG("RuntimeException thrown");
        }
    }
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_RenderTask_rtGetState(
        JNIEnv *env,
        jobject /*thiz*/
) {
    const auto res {static_cast<::std::int32_t> (state_.load())};
    env->ExceptionClear();
    return res;
}

extern "C"
float Java_puscas_mobilertapp_RenderTask_rtGetFps(
        JNIEnv *env,
        jobject /*thiz*/
) {
    env->ExceptionClear();
    return fps_;
}

extern "C"
jlong Java_puscas_mobilertapp_RenderTask_rtGetTimeRenderer(
        JNIEnv *env,
        jobject /*thiz*/
) {
    env->ExceptionClear();
    return timeRenderer_;
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_RenderTask_rtGetSample(
        JNIEnv *env,
        jobject /*thiz*/
) {
    ::std::int32_t sample {};
    {
        const ::std::lock_guard<::std::mutex> lock {mutex_};
        if (renderer_ != nullptr) {
            sample = renderer_->getSample();
        }
    }
    env->ExceptionClear();
    return sample;
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_MainActivity_rtResize(
        JNIEnv *env,
        jobject /*thiz*/,
        jint size
) {
    const auto res {
        ::MobileRT::roundDownToMultipleOf(
            size, static_cast<::std::int32_t> (::std::sqrt(::MobileRT::NumberOfTiles))
        )
    };
    env->ExceptionClear();
    return res;
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_DrawView_rtGetNumberOfLights(
        JNIEnv *env,
        jobject /*thiz*/
) {
    env->ExceptionClear();
    return numLights_;
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_rtFreeNativeBuffer(
        JNIEnv *env,
        jobject /*thiz*/,
        jobject bufferRef
) {
    if (bufferRef != nullptr) {
        auto *buffer {env->GetDirectBufferAddress(bufferRef)};
        float *const floatBuffer {static_cast<float*> (buffer)};
        delete[] floatBuffer;
    }
    return nullptr;
}
