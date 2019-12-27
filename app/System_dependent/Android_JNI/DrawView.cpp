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

static float fps_ {};
static ::std::atomic<::State> state_ {State::IDLE};
static ::std::unique_ptr<::MobileRT::Renderer> renderer_ {};
static ::std::unique_ptr<::JavaVM> javaVM_ {};
static ::std::unique_ptr<::std::thread> thread_ {};
static ::std::mutex mutex_ {};
static ::std::int32_t numLights_ {};
static ::std::int64_t timeRenderer_ {};
static ::std::condition_variable rendered_ {};
static ::std::atomic<bool> finishedRendering_ {};

extern "C"
::std::int32_t JNI_OnLoad(JavaVM *const jvm, void * /*reserved*/) {
    LOG("JNI_OnLoad");
    javaVM_.reset(jvm);

    JNIEnv *jniEnv {};
    {
        const ::std::int32_t result {javaVM_->GetEnv(reinterpret_cast<void **>(&jniEnv), JNI_VERSION_1_6)};
        assert(result == JNI_OK);
        static_cast<void> (result);
    }
    assert(jniEnv != nullptr);
    jniEnv->ExceptionClear();
    return JNI_VERSION_1_6;
}

extern "C"
void JNI_OnUnload(JavaVM *const /*jvm*/, void * /*reserved*/) {
    LOG("JNI_OnUnload");
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTInitCameraArray(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    jobject directBuffer{};
    {
        const ::std::lock_guard<::std::mutex> lock {mutex_};
        if (renderer_ != nullptr) {
            ::MobileRT::Camera *const camera {renderer_->camera_.get()};
            const ::std::uint64_t arraySize {20};
            const auto arrayBytes {static_cast<jlong> (arraySize) * static_cast<jlong> (sizeof(jfloat))};
            float *const floatBuffer {new float[arraySize]};

            if (floatBuffer != nullptr) {
                directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);
                if (directBuffer != nullptr) {
                    int i {};

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
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTInitVerticesArray(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    jobject directBuffer {};
    {
        const ::std::lock_guard<::std::mutex> lock {mutex_};
        if (renderer_ != nullptr) {
            const ::std::vector<::MobileRT::Primitive<::MobileRT::Triangle>> &triangles {
                renderer_->shader_->getTriangles()
            };
            const unsigned long arraySize {triangles.size() * 3 * 4};
            const auto arrayBytes {static_cast<jlong> (arraySize) * static_cast<jlong> (sizeof(jfloat))};
            if (arraySize > 0) {
                float *const floatBuffer {new float[arraySize]};
                if (floatBuffer != nullptr) {
                    directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);
                    if (directBuffer != nullptr) {
                        int i {};
                        for (const ::MobileRT::Primitive<::MobileRT::Triangle> &triangle : triangles) {
                            const ::glm::vec4 &pointA {triangle.shape_.pointA_.x,
                                                       triangle.shape_.pointA_.y,
                                                       triangle.shape_.pointA_.z, 1.0F};
                            const ::glm::vec4 &pointB {pointA.x + triangle.shape_.AB_.x,
                                                       pointA.y + triangle.shape_.AB_.y,
                                                       pointA.z + triangle.shape_.AB_.z, 1.0F};
                            const ::glm::vec4 &pointC {pointA.x + triangle.shape_.AC_.x,
                                                       pointA.y + triangle.shape_.AC_.y,
                                                       pointA.z + triangle.shape_.AC_.z, 1.0F};

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
        }
        env->ExceptionClear();
    }
    return directBuffer;
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTInitColorsArray(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    jobject directBuffer {};
    {
        const ::std::lock_guard<::std::mutex> lock {mutex_};
        if (renderer_ != nullptr) {
            const ::std::vector<::MobileRT::Primitive<::MobileRT::Triangle>> &triangles{
                renderer_->shader_->getTriangles()
            };
            const unsigned long arraySize {triangles.size() * 3 * 4};
            const auto arrayBytes {static_cast<jlong> (arraySize) * static_cast<jlong> (sizeof(jfloat))};
            if (arraySize > 0) {
                float *const floatBuffer {new float[arraySize]};
                if (floatBuffer != nullptr) {
                    directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);
                    if (directBuffer != nullptr) {
                        int i{};
                        for (const ::MobileRT::Primitive<::MobileRT::Triangle> &triangle : triangles) {
                            const ::glm::vec3 &kD {triangle.material_.Kd_};
                            const ::glm::vec3 &kS {triangle.material_.Ks_};
                            const ::glm::vec3 &kT {triangle.material_.Kt_};
                            const ::glm::vec3 &lE {triangle.material_.Le_};
                            ::glm::vec3 color {kD};

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
        }
        env->ExceptionClear();
    }
    return directBuffer;
}

static void updateFps() noexcept {
    static ::std::int32_t frame {};
    static ::std::chrono::steady_clock::time_point timebase {};
    ++frame;
    const ::std::chrono::steady_clock::time_point timeNow {::std::chrono::steady_clock::now()};
    const ::std::int64_t timeElapsed {
        ::std::chrono::duration_cast<std::chrono::milliseconds>(timeNow - timebase).count()
    };
    if (timeElapsed > 1000) {
        fps_ = (frame * 1000.0F) / timeElapsed;
        timebase = timeNow;
        frame = 0;
    }
}

extern "C"
void Java_puscas_mobilertapp_DrawView_RTStartRender(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    finishedRendering_ = false;
    state_ = State::BUSY;
    LOG("STATE = BUSY");
    env->ExceptionClear();
}

extern "C"
void Java_puscas_mobilertapp_DrawView_RTStopRender(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    //TODO: Fix this race condition
    while (!finishedRendering_ && renderer_ != nullptr) {
        state_ = State::STOPPED;
        LOG("STATE = STOPPED");
        renderer_->stopRender();
    }
    state_ = State::STOPPED;
    LOG("STATE = STOPPED");
    {
        ::std::unique_lock<std::mutex> lock {mutex_};
        while (!finishedRendering_){
            rendered_.wait(lock);
            if (renderer_ != nullptr) {
                renderer_->stopRender();
            }
        }
    }
    env->ExceptionClear();
    LOG("stopRender finished");
}

static ::std::streampos fileSize(const char *filePath) {
    ::std::ifstream file {filePath, ::std::ios::binary};

    const ::std::streampos fileBegin {file.tellg()};
    file.seekg(0, ::std::ios::end);
    const ::std::streampos fileSize {file.tellg() - fileBegin};
    file.close();

    return fileSize;
}

extern "C"
jint Java_puscas_mobilertapp_MainRenderer_RTInitialize(
        JNIEnv *env,
        jobject thiz,
        jint scene,
        jint shader,
        jint width,
        jint height,
        jint accelerator,
        jint samplesPixel,
        jint samplesLight,
        jstring localObjFile,
        jstring localMatFile,
        jstring localCamFile
) noexcept {
    LOG("INITIALIZE");
    const jclass rendererClass {env->FindClass("puscas/mobilertapp/MainRenderer")};
    const jmethodID isLowMemoryMethodId {env->GetMethodID(rendererClass, "isLowMemory", "(I)Z")};
    const auto globalObjFile {static_cast<jstring>(env->NewGlobalRef(localObjFile))};
    const auto globalMatFile {static_cast<jstring>(env->NewGlobalRef(localMatFile))};
    const auto globalCamFile {static_cast<jstring>(env->NewGlobalRef(localCamFile))};

    const ::std::int32_t res {
        [&]() noexcept -> ::std::int32_t {
            const ::std::lock_guard<::std::mutex> lock {mutex_};
            renderer_ = nullptr;
            const auto ratio {static_cast<float>(width) / height};
            ::MobileRT::Scene scene_ {};
            ::std::unique_ptr<::MobileRT::Sampler> samplerPixel {};
            ::std::unique_ptr<::MobileRT::Shader> shader_ {};
            ::std::unique_ptr<::MobileRT::Camera> camera {};
            ::glm::vec3 maxDist {};
            switch (scene) {
                case 0: {
                    const float fovX {45.0F * ratio};
                    const float fovY {45.0F};
                    camera = ::std::make_unique<Components::Perspective>(
                        ::glm::vec3 {0.0F, 0.0F, -3.4F},
                        ::glm::vec3 {0.0F, 0.0F, 1.0F},
                        ::glm::vec3 {0.0F, 1.0F, 0.0F},
                        fovX, fovY
                    );
                    scene_ = cornellBoxScene(::std::move(scene_));
                    maxDist = ::glm::vec3{1, 1, 1};
                }
                    break;

                case 1: {
                    const float sizeH {10.0F * ratio};
                    const float sizeV {10.0F};
                    camera = ::std::make_unique<Components::Orthographic>(
                        ::glm::vec3 {0.0F, 1.0F, -10.0F},
                        ::glm::vec3 {0.0F, 1.0F, 7.0F},
                        ::glm::vec3 {0.0F, 1.0F, 0.0F},
                        sizeH, sizeV
                    );
                    scene_ = spheresScene(::std::move(scene_));
                    maxDist = ::glm::vec3{8, 8, 8};
                }
                    break;

                case 2: {
                    const float fovX {45.0F * ratio};
                    const float fovY {45.0F};
                    camera = ::std::make_unique<Components::Perspective>(
                        ::glm::vec3{0.0F, 0.0F, -3.4F},
                        ::glm::vec3{0.0F, 0.0F, 1.0F},
                        ::glm::vec3{0.0F, 1.0F, 0.0F},
                        fovX, fovY
                    );
                    scene_ = cornellBoxScene2(::std::move(scene_));
                    maxDist = ::glm::vec3{1, 1, 1};
                }
                    break;

                case 3: {
                    const float fovX {60.0F * ratio};
                    const float fovY {60.0F};
                    camera = ::std::make_unique<Components::Perspective>(
                        ::glm::vec3{0.0F, 0.5F, 1.0F},
                        ::glm::vec3{0.0F, 0.0F, 7.0F},
                        ::glm::vec3{0.0F, 1.0F, 0.0F},
                        fovX, fovY
                    );
                    scene_ = spheresScene2(::std::move(scene_));
                    maxDist = ::glm::vec3 {8, 8, 8};
                }
                    break;

                default: {
                    jboolean isCopy{JNI_FALSE};
                    const char *const objFilePath {env->GetStringUTFChars(globalObjFile, &isCopy)};
                    const char *const matFilePath {env->GetStringUTFChars(globalMatFile, &isCopy)};
                    const char *const camFilePath {env->GetStringUTFChars(globalCamFile, &isCopy)};

                    assert(isLowMemoryMethodId != nullptr);
                    assert(objFilePath != nullptr);
                    assert(matFilePath != nullptr);
                    assert(camFilePath != nullptr);
                    {
                        const jboolean lowMem {env->CallBooleanMethod(thiz, isLowMemoryMethodId, 1)};
                        if (lowMem) {
                            return -1;
                        }
                    }

                    const auto cameraFactory {::Components::CameraFactory()};
                    camera = cameraFactory.loadFromFile(camFilePath, ratio);

                    ::Components::OBJLoader objLoader{objFilePath, matFilePath};
                    {
                        const jboolean lowMem {env->CallBooleanMethod(thiz, isLowMemoryMethodId, 1)};
                        if (lowMem) {
                            return -1;
                        }
                    }

                    const auto objSize {fileSize(objFilePath)};
                    if (objSize <= 0) {
                        finishedRendering_ = true;
                        return -2;
                    }
                    {
                        const auto neededMemoryMb {1 + static_cast<::std::int32_t> (3 * (objSize / 1048576))};
                        const jboolean lowMem {env->CallBooleanMethod(thiz, isLowMemoryMethodId, neededMemoryMb)};
                        if (lowMem) {
                            finishedRendering_ = true;
                            return -1;
                        }
                    }
                    const ::std::int32_t triangleSize {sizeof(::MobileRT::Triangle)};
                    const ::std::int32_t bvhNodeSize {sizeof(::MobileRT::BVHNode)};
                    const ::std::int32_t aabbSize {sizeof(::MobileRT::AABB)};
                    const ::std::int32_t floatSize {sizeof(float)};
                    const ::std::int32_t numberPrimitives {objLoader.process()};
                    const ::std::int32_t memPrimitives {(numberPrimitives * triangleSize) / 1048576};
                    const ::std::int32_t memNodes {(2 * numberPrimitives * bvhNodeSize) / 1048576};
                    const ::std::int32_t memAABB {(2 * numberPrimitives * aabbSize) / 1048576};
                    const ::std::int32_t memFloat {(2 * numberPrimitives * floatSize) / 1048576};
                    const ::std::int32_t neededMemoryMb {1 + memPrimitives + memNodes + memAABB + memFloat};
                    {
                        const jboolean lowMem {env->CallBooleanMethod(thiz, isLowMemoryMethodId, neededMemoryMb)};
                        if (lowMem) {
                            return -1;
                        }
                    }

                    if (!objLoader.isProcessed()) {
                        return -1;
                    }
                    const bool sceneBuilt {objLoader.fillScene(
                            &scene_, []() {return ::std::make_unique<Components::StaticHaltonSeq>();}
                    )};
                    if (!sceneBuilt) {
                        return -1;
                    }
                    {
                        const jboolean lowMem {env->CallBooleanMethod(thiz, isLowMemoryMethodId, 1)};
                        if (lowMem) {
                            return -1;
                        }
                    }

                    maxDist = ::glm::vec3 {1, 1, 1};

                    env->ReleaseStringUTFChars(globalObjFile, objFilePath);
                    env->ReleaseStringUTFChars(globalMatFile, matFilePath);
                    env->ReleaseStringUTFChars(globalCamFile, camFilePath);
                }
                    break;
            }
            samplerPixel = samplesPixel <= 1
                ? ::std::unique_ptr<::MobileRT::Sampler> (::std::make_unique<Components::Constant> (0.5F))
                : ::std::unique_ptr<::MobileRT::Sampler> (::std::make_unique<Components::StaticHaltonSeq> ());
            const ::std::chrono::time_point<::std::chrono::system_clock> start {
                    ::std::chrono::system_clock::now()
            };
            switch (shader) {
                case 1: {
                    shader_ = ::std::make_unique<Components::Whitted>(
                            ::std::move(scene_),
                            static_cast<::std::uint32_t> (samplesLight),
                            ::MobileRT::Shader::Accelerator(accelerator)
                    );
                    break;
                }

                case 2: {
                    ::std::unique_ptr<MobileRT::Sampler> samplerRussianRoulette {
                            ::std::make_unique<Components::StaticHaltonSeq>()
                    };

                    shader_ = ::std::make_unique<Components::PathTracer>(
                            ::std::move(scene_),
                            ::std::move(samplerRussianRoulette),
                            static_cast<::std::uint32_t> (samplesLight),
                            ::MobileRT::Shader::Accelerator(accelerator)
                    );
                    break;
                }

                case 3: {
                    shader_ = ::std::make_unique<Components::DepthMap>(
                            ::std::move(scene_), maxDist, ::MobileRT::Shader::Accelerator(accelerator)
                    );
                    break;
                }

                case 4: {
                    shader_ = ::std::make_unique<Components::DiffuseMaterial>(
                            ::std::move(scene_), ::MobileRT::Shader::Accelerator(accelerator)
                    );
                    break;
                }

                default: {
                    shader_ = ::std::make_unique<Components::NoShadows>(
                            ::std::move(scene_),
                            static_cast<::std::uint32_t> (samplesLight),
                            ::MobileRT::Shader::Accelerator(accelerator)
                    );
                    break;
                }
            }
            const ::std::chrono::time_point<::std::chrono::system_clock> end {
                    ::std::chrono::system_clock::now()
            };
            const auto planes {static_cast<::std::int32_t> (shader_->getPlanes().size())};
            const auto spheres {static_cast<::std::int32_t> (shader_->getSpheres().size())};
            const auto triangles {static_cast<::std::int32_t> (shader_->getTriangles().size())};
            numLights_ = static_cast<::std::int32_t> (shader_->getLights().size());
            const ::std::int32_t nPrimitives {triangles + spheres + planes};
            renderer_ = ::std::make_unique<::MobileRT::Renderer>(
                    ::std::move(shader_), ::std::move(camera), ::std::move(samplerPixel),
                    static_cast<::std::uint32_t>(width), static_cast<::std::uint32_t>(height),
                    static_cast<::std::uint32_t>(samplesPixel)
            );
            timeRenderer_ = ::std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
            LOG("TIME CONSTRUCTION RENDERER = ", timeRenderer_, "ms");
            /*LOG("TRIANGLES = ", triangles);
            LOG("SPHERES = ", spheres);
            LOG("PLANES = ", planes);
            LOG("LIGHTS = ", numLights_);*/
            return nPrimitives;
        }()};


    env->ExceptionClear();
    LOG("PRIMITIVES = ", res);

    {
        const jboolean lowMem {env->CallBooleanMethod(thiz, isLowMemoryMethodId, 1)};
        if (lowMem) {
            return -1;
        }
    }

    return res;
}

extern "C"
void Java_puscas_mobilertapp_MainRenderer_RTFinishRender(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    //TODO: Fix this race condition
    {
        const ::std::lock_guard<::std::mutex> lock {mutex_};
        state_ = State::FINISHED;
        LOG("STATE = FINISHED");
        if (renderer_ != nullptr) {
            renderer_->stopRender();
        }
        if (thread_ != nullptr) {
            renderer_ = nullptr;
            thread_ = nullptr;
            LOG("DELETED RENDERER");
        }
        state_ = State::IDLE;
        LOG("STATE = IDLE");
        fps_ = 0.0F;
        timeRenderer_ = 0;
        env->ExceptionClear();
    }
}

extern "C"
void Java_puscas_mobilertapp_MainRenderer_RTRenderIntoBitmap(
        JNIEnv *env,
        jobject /*thiz*/,
        jobject localBitmap,
        jint nThreads,
        jboolean async
) noexcept {
    auto globalBitmap {static_cast<jobject>(env->NewGlobalRef(localBitmap))};

    auto lambda {
        [=]() noexcept -> void {
            assert(env != nullptr);
            const ::std::int32_t jniError {
                    javaVM_->GetEnv(reinterpret_cast<void **>(const_cast<JNIEnv **>(&env)), JNI_VERSION_1_6)
            };

            assert(jniError == JNI_OK || jniError == JNI_EDETACHED);
            {
                const ::std::int32_t result {javaVM_->AttachCurrentThread(const_cast<JNIEnv **>(&env), nullptr)};
                assert(result == JNI_OK);
                static_cast<void>(result);
            }

            ::std::uint32_t *dstPixels{};
            {
                const ::std::int32_t ret {
                    AndroidBitmap_lockPixels(env, globalBitmap, reinterpret_cast<void **>(&dstPixels))
                };
                //dstPixels = static_cast<::std::uint32_t *>(env->GetDirectBufferAddress(globalByteBuffer));
                assert(ret == JNI_OK);
                LOG("ret = ", ret);
            }

            AndroidBitmapInfo info{};
            {
                const ::std::int32_t ret {AndroidBitmap_getInfo(env, globalBitmap, &info)};
                assert(ret == JNI_OK);
                LOG("ret = ", ret);
            }

            const ::std::uint32_t stride {info.stride};
            ::std::int32_t rep {1};
            while (state_ == State::BUSY && rep > 0) {
                LOG("STARTING RENDERING");
                LOG("nThreads = ", nThreads);
                {
                    const ::std::lock_guard<::std::mutex> lock {mutex_};
                    rendered_.notify_all();
                    if (renderer_ != nullptr) {
                        renderer_->renderFrame(dstPixels, nThreads, stride);
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
                    const ::std::int32_t result {AndroidBitmap_unlockPixels(env, globalBitmap)};
                    assert(result == JNI_OK);
                    static_cast<void> (result);
                }

                env->DeleteGlobalRef(globalBitmap);
                {
                    const ::std::int32_t result {
                        javaVM_->GetEnv(reinterpret_cast<void **>(const_cast<JNIEnv **>(&env)), JNI_VERSION_1_6)
                    };
                    assert(result == JNI_OK);
                    static_cast<void> (result);
                }
                env->ExceptionClear();
                if (jniError == JNI_EDETACHED) {
                    const ::std::int32_t result {javaVM_->DetachCurrentThread()};
                    assert(result == JNI_OK);
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
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_RenderTask_RTGetState(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    const auto res {static_cast<::std::int32_t> (state_.load())};
    env->ExceptionClear();
    return res;
}

extern "C"
float Java_puscas_mobilertapp_RenderTask_RTGetFps(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    env->ExceptionClear();
    return fps_;
}

extern "C"
jlong Java_puscas_mobilertapp_RenderTask_RTGetTimeRenderer(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    env->ExceptionClear();
    return timeRenderer_;
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_RenderTask_RTGetSample(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    ::std::uint32_t sample {};
    {
        //const ::std::lock_guard<::std::mutex> lock {mutex_};
        //TODO: Fix this race condition
        if (renderer_ != nullptr) {
            sample = renderer_->getSample();
        }
    }
    env->ExceptionClear();
    const auto res {static_cast<::std::int32_t> (sample)};
    return res;
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_MainActivity_RTResize(
        JNIEnv *env,
        jobject /*thiz*/,
        jint size
) noexcept {
    const ::std::int32_t res {
        ::MobileRT::roundDownToMultipleOf(
            size, static_cast<::std::int32_t>(::std::sqrt(::MobileRT::NumberOfBlocks))
        )
    };
    env->ExceptionClear();
    return res;
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_DrawView_RTGetNumberOfLights(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    env->ExceptionClear();
    return numLights_;
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTFreeNativeBuffer(
        JNIEnv *env,
        jobject /*thiz*/,
        jobject bufferRef
) noexcept {
    if (bufferRef != nullptr) {
        void *buffer {env->GetDirectBufferAddress(bufferRef)};
        float *const floatBuffer {static_cast<float *>(buffer)};
        delete[] floatBuffer;
    }
    return nullptr;
}
