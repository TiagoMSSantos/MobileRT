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
#include <fstream>
#include <istream>
#include <glm/glm.hpp>
#include <mutex>
#include <string>
#include <unistd.h>

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
 * The definition of the OBJ file.
 */
static ::std::string objDefinition_ {};

/**
 * The definition of the MTL file.
 */
static ::std::string mtlDefinition_ {};

/**
 * The definition of the CAM file.
 */
static ::std::string camDefinition_ {};

/**
 * The cache for textures.
 */
static ::std::unordered_map<::std::string, ::MobileRT::Texture> texturesCache_ {};


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
    const jclass clazz {env->FindClass(exceptionName)};
    const jint res {env->ThrowNew(clazz, exception.what())};
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
        const jint result {javaVM_->GetEnv(reinterpret_cast<void **> (&jniEnv), JNI_VERSION_1_6)};
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
                const jlong arrayBytes {arraySize * sizeof(jfloat)};

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

                        const ::Components::Perspective *const perspective {dynamic_cast<::Components::Perspective *> (camera)};
                        const ::Components::Orthographic *const orthographic {dynamic_cast<::Components::Orthographic *> (camera)};
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
                        if (perspective == nullptr && orthographic == nullptr) {
                            const ::std::string errorMessage {"Scene provided to MobileRT doesn't have any type of camera."};
                            throw ::std::runtime_error {errorMessage};
                        }
                    } else {
                        const ::std::string errorMessage {"JNIEnv::NewDirectByteBuffer failed to allocate native memory!"};
                        throw ::std::runtime_error {errorMessage};
                    }
                } else {
                    const ::std::string errorMessage {"C++ failed to allocate native memory!"};
                    throw ::std::runtime_error {errorMessage};
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
    MobileRT::checkSystemError("rtInitVerticesArray start");
    try {
        jobject directBuffer {};
        {
            const ::std::lock_guard<::std::mutex> lock {mutex_};
            if (renderer_ != nullptr) {
                const ::std::vector<::MobileRT::Triangle> &triangles {renderer_->shader_->getTriangles()};
                const ::std::uint32_t arraySize {static_cast<::std::uint32_t> (triangles.size() * 3 * 4)};
                const jlong arrayBytes {arraySize * static_cast<jlong> (sizeof(jfloat))};

                float *const floatBuffer {new float[arraySize]};

                if (floatBuffer != nullptr) {
                    directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);
                    if (directBuffer != nullptr) {
                        ::std::int32_t i {};
                        for (const ::MobileRT::Triangle &triangle : triangles) {
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
                    } else {
                        const ::std::string errorMessage {"JNIEnv::NewDirectByteBuffer failed to allocate native memory!"};
                        throw ::std::runtime_error {errorMessage};
                    }
                } else {
                    const ::std::string errorMessage {"C++ failed to allocate native memory!"};
                    throw ::std::runtime_error {errorMessage};
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
        jobject directBuffer {};
        {
            const ::std::lock_guard<::std::mutex> lock {mutex_};
            if (renderer_ != nullptr) {
                const ::std::vector<::MobileRT::Triangle> &triangles {renderer_->shader_->getTriangles()};
                const ::std::uint32_t arraySize {static_cast<::std::uint32_t> (triangles.size() * 3 * 4)};
                const jlong arrayBytes {arraySize * static_cast<::std::int64_t> (sizeof(jfloat))};

                float *const floatBuffer {new float[arraySize]};

                if (floatBuffer != nullptr) {
                    directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);
                    if (directBuffer != nullptr) {
                        ::std::int32_t i {};
                        for (const ::MobileRT::Triangle &triangle : triangles) {
                            const ::std::int32_t materialIndex {triangle.getMaterialIndex()};
                            ::MobileRT::Material material {::MobileRT::Material{}};
                            if (materialIndex >= 0) {
                                material = renderer_->shader_->getMaterials()[static_cast<::std::uint32_t> (materialIndex)];
                            }

                            const ::glm::vec3 &kD{material.Kd_};
                            const ::glm::vec3 &kS{material.Ks_};
                            const ::glm::vec3 &kT{material.Kt_};
                            const ::glm::vec3 &lE{material.Le_};
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
                    } else {
                        const ::std::string errorMessage {"JNIEnv::NewDirectByteBuffer failed to allocate native memory!"};
                        throw ::std::runtime_error {errorMessage};
                    }
                } else {
                    const ::std::string errorMessage {"C++ failed to allocate native memory!"};
                    throw ::std::runtime_error {errorMessage};
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
    const ::std::chrono::steady_clock::time_point timeNow {::std::chrono::steady_clock::now()};
    const long long timeElapsed {::std::chrono::duration_cast<::std::chrono::milliseconds>(timeNow - timebase).count()};
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
    LOG_DEBUG("rtStopRender finished");
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
        const jclass configClass {env->GetObjectClass(localConfig)};

        const jmethodID sceneMethodId {env->GetMethodID(configClass, "getScene", "()I")};
        const jint sceneIndex {env->CallIntMethod(localConfig, sceneMethodId)};
        LOG_DEBUG("sceneIndex: ", sceneIndex);

        const jmethodID shaderMethodId {env->GetMethodID(configClass, "getShader", "()I")};
        const jint shaderIndex {env->CallIntMethod(localConfig, shaderMethodId)};
        LOG_DEBUG("shaderIndex: ", shaderIndex);

        const jmethodID acceleratorMethodId {env->GetMethodID(configClass, "getAccelerator", "()I")};
        const jint acceleratorIndex {env->CallIntMethod(localConfig, acceleratorMethodId)};
        LOG_DEBUG("acceleratorIndex: ", acceleratorIndex);


        const jmethodID configResolutionMethodId {env->GetMethodID(configClass, "getConfigResolution",
                                                         "()Lpuscas/mobilertapp/configs/ConfigResolution;")};
        const jobject resolutionConfig {env->CallObjectMethod(localConfig, configResolutionMethodId)};
        const jclass resolutionConfigClass {env->GetObjectClass(resolutionConfig)};

        const jmethodID widthMethodId {env->GetMethodID(resolutionConfigClass, "getWidth", "()I")};
        const jint width {env->CallIntMethod(resolutionConfig, widthMethodId)};
        LOG_DEBUG("width: ", width);

        const jmethodID heightMethodId {env->GetMethodID(resolutionConfigClass, "getHeight", "()I")};
        const jint height {env->CallIntMethod(resolutionConfig, heightMethodId)};
        LOG_DEBUG("height: ", height);


        const jmethodID configSamplesMethodId {env->GetMethodID(configClass, "getConfigSamples",
                                                      "()Lpuscas/mobilertapp/configs/ConfigSamples;")};
        const jobject samplesConfig {env->CallObjectMethod(localConfig, configSamplesMethodId)};
        const jclass samplesConfigClass {env->GetObjectClass(samplesConfig)};

        const jmethodID samplesPixelMethodId {env->GetMethodID(samplesConfigClass, "getSamplesPixel", "()I")};
        const jint samplesPixel {env->CallIntMethod(samplesConfig, samplesPixelMethodId)};
        LOG_DEBUG("samplesPixel: ", samplesPixel);

        const jmethodID samplesLightMethodId {env->GetMethodID(samplesConfigClass, "getSamplesLight", "()I")};
        const jint samplesLight {env->CallIntMethod(samplesConfig, samplesLightMethodId)};
        LOG_DEBUG("samplesLight: ", samplesLight);

        jboolean isCopy {JNI_FALSE};
        const jmethodID objMethodId {env->GetMethodID(configClass, "getObjFilePath", "()Ljava/lang/String;")};
        const jstring localObjFilePath {reinterpret_cast<jstring> (env->CallObjectMethod(localConfig, objMethodId))};
        const ::std::string objFilePath {env->GetStringUTFChars(localObjFilePath, &isCopy)};
        LOG_DEBUG("objFilePath: ", objFilePath);

        const ::std::int32_t res {
            [&]() -> ::std::int32_t {
                LOG_DEBUG("Acquiring lock");
                const ::std::lock_guard<::std::mutex> lock {mutex_};
                renderer_ = nullptr;
                const float ratio {static_cast<float> (width) / static_cast<float> (height)};
                ::MobileRT::Scene scene {};
                ::std::unique_ptr<::MobileRT::Sampler> samplerPixel {};
                ::std::unique_ptr<::MobileRT::Shader> shader {};
                ::std::unique_ptr<::MobileRT::Camera> camera {};
                ::glm::vec3 maxDist {};
                LOG_DEBUG("LOADING SCENE: ", sceneIndex);
                switch (sceneIndex) {
                    case 0:
                        scene = cornellBox_Scene(::std::move(scene));
                        camera = cornellBox_Cam(ratio);
                        maxDist = ::glm::vec3 {1, 1, 1};
                        break;

                    case 1:
                        scene = spheres_Scene(::std::move(scene));
                        camera = spheres_Cam(ratio);
                        maxDist = ::glm::vec3 {8, 8, 8};
                        break;

                    case 2:
                        scene = cornellBox2_Scene(::std::move(scene));
                        camera = cornellBox_Cam(ratio);
                        maxDist = ::glm::vec3 {1, 1, 1};
                        break;

                    case 3:
                        scene = spheres2_Scene(::std::move(scene));
                        camera = spheres2_Cam(ratio);
                        maxDist = ::glm::vec3 {8, 8, 8};
                        break;

                    default: {
                        if (objDefinition_.empty()) {
                            LOG_DEBUG("OBJ file not read!");
                            throw ::std::runtime_error {"OBJ file not read!"};
                        }
                        if (mtlDefinition_.empty()) {
                            LOG_DEBUG("MTL file not read!");
                        }
                        if (camDefinition_.empty()) {
                            LOG_DEBUG("CAM file not read!");
                        }

                        ::Components::CameraFactory cameraFactory {::Components::CameraFactory()};
                        const ::std::istringstream isCam {camDefinition_};
                        ::std::istream iCam {isCam.rdbuf()};
                        camera = cameraFactory.loadFromFile(iCam, ratio);

                        const ::std::istringstream isObj {objDefinition_};
                        const ::std::istringstream isMtl {mtlDefinition_};
                        ::std::istream iObj {isObj.rdbuf()};
                        ::std::istream iMtl {isMtl.rdbuf()};
                        ::Components::OBJLoader objLoader {iObj, iMtl};
                        objDefinition_.clear();
                        mtlDefinition_.clear();
                        camDefinition_.clear();
                        iObj.clear();
                        iMtl.clear();
                        objDefinition_.erase();
                        mtlDefinition_.erase();
                        camDefinition_.erase();
                        objDefinition_.shrink_to_fit();
                        mtlDefinition_.shrink_to_fit();
                        camDefinition_.shrink_to_fit();

                        MobileRT::checkSystemError("rtInitialize after loading OBJ");
                        LOG_DEBUG("OBJLOADER PROCESSED");

                        if (!objLoader.isProcessed()) {
                            LOG_ERROR("OBJLOADER could not load the scene.");
                            return -1;
                        }
                        const bool sceneBuilt {objLoader.fillScene(
                            &scene,
                            []() {return ::MobileRT::std::make_unique<Components::StaticPCG>();},
                            objFilePath,
                            texturesCache_
                        )};
                        texturesCache_.clear();
                        MobileRT::checkSystemError("rtInitialize after filling scene");
                        if (!sceneBuilt) {
                            LOG_ERROR("OBJLOADER could not load the scene.");
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
                const ::std::chrono::time_point<::std::chrono::system_clock> chronoStart {::std::chrono::system_clock::now()};
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
                const ::std::chrono::time_point<::std::chrono::system_clock> chronoEnd {::std::chrono::system_clock::now()};
                MobileRT::checkSystemError("rtInitialize after loading shader");

                LOG_DEBUG("LOADING RENDERER");
                const ::std::int32_t planes {static_cast<::std::int32_t> (shader->getPlanes().size())};
                const ::std::int32_t spheres {static_cast<::std::int32_t> (shader->getSpheres().size())};
                const ::std::int32_t triangles {static_cast<::std::int32_t> (shader->getTriangles().size())};
                const ::std::int32_t materials {static_cast<::std::int32_t> (shader->getMaterials().size())};
                numLights_ = static_cast<::std::int32_t> (shader->getLights().size());
                const ::std::int32_t nPrimitives {triangles + spheres + planes};
                LOG_INFO("PLANES = ", planes);
                LOG_INFO("SPHERES = ", spheres);
                LOG_INFO("TRIANGLES = ", triangles);
                LOG_INFO("LIGHTS = ", numLights_);
                LOG_INFO("MATERIALS = ", materials);
                LOG_INFO("TOTAL PRIMITIVES = ", nPrimitives);
                LOG_INFO("width = ", width);
                LOG_INFO("height = ", height);
                LOG_INFO("samplesPixel = ", samplesPixel);
                renderer_ = ::MobileRT::std::make_unique<::MobileRT::Renderer>(
                    ::std::move(shader), ::std::move(camera), ::std::move(samplerPixel),
                    width, height, samplesPixel
                );
                MobileRT::checkSystemError("Renderer was built.");
                timeRenderer_ = ::std::chrono::duration_cast<::std::chrono::milliseconds>(chronoEnd - chronoStart).count();
                LOG_INFO("TIME CONSTRUCTION RENDERER = ", timeRenderer_, "ms");
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
        handleException(env, ::std::exception {}, "java/lang/RuntimeException");
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
            LOG_DEBUG("RENDERER STOP");
            renderer_->stopRender();
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
        jobject globalBitmap {static_cast<jobject> (env->NewGlobalRef(localBitmap))};

        const ::std::function<void()> lambda {
            [=]() -> void {
                ::std::chrono::duration<double> timeRendering {};

                LOG_DEBUG("rtRenderIntoBitmap step 1");
                ASSERT(env != nullptr, "JNIEnv not valid.");
                MobileRT::checkSystemError("rtRenderIntoBitmap step 1");
                const jint jniError {
                    javaVM_->GetEnv(reinterpret_cast<void **> (const_cast<JNIEnv **> (&env)), JNI_VERSION_1_6)
                };

                LOG_DEBUG("rtRenderIntoBitmap step 2: ", jniError);
                ASSERT(jniError == JNI_OK || jniError == JNI_EDETACHED, "JNIEnv not valid.");
                static_cast<void>(jniError);
                {
                    MobileRT::checkSystemError("rtRenderIntoBitmap step 2");
                    const jint result {javaVM_->AttachCurrentThread(const_cast<JNIEnv **> (&env), nullptr)};
                    if (errno == EINVAL) {
                        // Ignore invalid argument (necessary for Android API 16)
                        errno = 0;
                    }
                    MobileRT::checkSystemError("rtRenderIntoBitmap step 3");
                    ASSERT(result == JNI_OK, "Couldn't attach current thread to JVM.");
                    static_cast<void> (result);
                }

                LOG_DEBUG("rtRenderIntoBitmap step 3");
                ::std::int32_t *dstPixels {};
                {
                    const jint ret {
                        AndroidBitmap_lockPixels(env, globalBitmap,
                                                 reinterpret_cast<void **> (&dstPixels))
                    };
                    ASSERT(ret == JNI_OK, "Couldn't lock the Android bitmap pixels.");
                    LOG_DEBUG("ret = ", ret);
                    static_cast<void> (ret);
                }

                LOG_DEBUG("rtRenderIntoBitmap step 4");
                AndroidBitmapInfo info {};
                {
                    MobileRT::checkSystemError("rtRenderIntoBitmap step 4");
                    const jint ret {AndroidBitmap_getInfo(env, globalBitmap, &info)};
                    ASSERT(ret == JNI_OK, "Couldn't get the Android bitmap information structure.");
                    LOG_DEBUG("ret = ", ret);
                    static_cast<void> (ret);
                }

                LOG_DEBUG("rtRenderIntoBitmap step 5");
                ::std::int32_t rep {1};
                {
                    const jint result {javaVM_->DetachCurrentThread()};
                    ASSERT(result == JNI_OK, "Couldn't detach the current thread from JVM.");
                    static_cast<void> (result);
                }
                LOG_DEBUG("WILL START TO RENDER");
                MobileRT::checkSystemError("starting render timer");
                const ::std::chrono::time_point<::std::chrono::system_clock> chronoStartRendering {::std::chrono::system_clock::now()};
                while (state_ == State::BUSY && rep > 0) {
                    LOG_DEBUG("STARTING RENDERING");
                    LOG_DEBUG("nThreads = ", nThreads);
                    {
                        if (renderer_ != nullptr) {
                            MobileRT::checkSystemError("starting renderFrame");
                            renderer_->renderFrame(dstPixels, nThreads);
                            MobileRT::checkSystemError("renderFrame done");
                        }
                    }
                    LOG_DEBUG("FINISHED RENDERING");
                    updateFps();
                    rep--;
                }
                const ::std::chrono::time_point<::std::chrono::system_clock> chronoEndRendering {::std::chrono::system_clock::now()};
                timeRendering = chronoEndRendering - chronoStartRendering;
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
                        MobileRT::checkSystemError("rtRenderIntoBitmap step 6");
                        const jint result {javaVM_->AttachCurrentThread(const_cast<JNIEnv **> (&env), nullptr)};
                        if (errno == EINVAL) {
                            // Ignore invalid argument (necessary for Android API 16)
                            errno = 0;
                        }
                        MobileRT::checkSystemError("rtRenderIntoBitmap step 7");
                        ASSERT(result == JNI_OK, "Couldn't attach current thread to JVM.");
                        static_cast<void> (result);
                    }
                    {
                        const jint result{AndroidBitmap_unlockPixels(env, globalBitmap)};
                        ASSERT(result == JNI_OK, "Couldn't unlock the Android bitmap pixels.");
                        static_cast<void> (result);
                    }

                    env->DeleteGlobalRef(globalBitmap);
                    {
                        const jint result {
                            javaVM_->GetEnv(reinterpret_cast<void **> (const_cast<JNIEnv **> (&env)), JNI_VERSION_1_6)
                        };
                        ASSERT(result == JNI_OK, "JNIEnv not valid.");
                        static_cast<void> (result);
                    }
                    env->ExceptionClear();
                    {
                        const jint result {javaVM_->DetachCurrentThread()};
                        ASSERT(result == JNI_OK, "Couldn't detach the current thread from JVM.");
                        static_cast<void> (result);
                    }
                }
                const double renderingTime {timeRendering.count()};
                const ::std::uint64_t castedRays {renderer_->getTotalCastedRays()};
                LOG_INFO("Rendering Time in secs = ", renderingTime);
                LOG_INFO("Casted rays = ", castedRays);
                LOG_INFO("Total Millions rays per second = ", (static_cast<double> (castedRays) / renderingTime) / 1000000L);

                state_ = State::IDLE;
                LOG_DEBUG("rtRenderIntoBitmap finished");
            }
        };

        MobileRT::checkSystemError("rtRenderIntoBitmap creating thread");
        thread_ = ::MobileRT::std::make_unique<::std::thread>(lambda);
        if (errno == EINVAL) {
            // Ignore invalid argument (necessary for Android API 16)
            errno = 0;
        }
        MobileRT::checkSystemError("rtRenderIntoBitmap detaching thread");
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

    const ::std::int32_t res {static_cast<::std::int32_t> (state_.load())};
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
    ::std::int32_t sample {};
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

    const ::std::int32_t res{
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
JNIEXPORT
void JNICALL Java_puscas_mobilertapp_MainActivity_readFile(
        JNIEnv *env,
        jobject /*thiz*/,
        jint fileDescriptor,
        jlong fileSize,
        jstring jFilePath
) {
    if (errno == EACCES || errno == ENOTSOCK || errno == EPERM || errno == ENOENT) {
        // Ignore permission denied before reading the file.
        // Ignore socket operation on non-socket.
        // Ignore operation not permitted.
        // Ignore no such file or directory.
        errno = 0;
    }
    jboolean isCopy {JNI_FALSE};
    const ::std::string filePathRaw {env->GetStringUTFChars(jFilePath, &isCopy)};
    const ::std::string typeStr {filePathRaw.substr(filePathRaw.find_last_of("."), filePathRaw.size())};
    const int type {
          typeStr == ".obj" ? 0
        : typeStr == ".mtl" ? 1
        : typeStr == ".cam" ? 2
        : 3
    };
    LOG_DEBUG("Will read a file natively.");
    ::std::string *file {nullptr};
    switch (type) {
        case 0:
            file = &objDefinition_;
            break;

        case 1:
            file = &mtlDefinition_;
            break;

        case 2:
            file = &camDefinition_;
            break;

        default:
            file = nullptr;
    }

    ASSERT(fileDescriptor > 2, "File descriptor not valid.");
    ASSERT(fileSize > 0, "File size not valid.");

    if (file != nullptr) {
        LOG_DEBUG("Will read a scene file.");
        file->resize(static_cast<::std::size_t> (fileSize));
        MobileRT::checkSystemError("Before read file.");
        const long remainingLength {::read(fileDescriptor, &(*file)[0], static_cast<unsigned int>(fileSize))};
        MobileRT::checkSystemError("After read file.");
        ASSERT(remainingLength == 0 || remainingLength == fileSize, "File not read entirely.");
        LOG_DEBUG("Read a scene file.");
        static_cast<void>(remainingLength);
    } else {
        LOG_DEBUG("Will read a texture file.");
        ::std::string texture {};
        texture.resize(static_cast<::std::size_t> (fileSize));
        MobileRT::checkSystemError("Before read file.");
        const long remainingLength {::read(fileDescriptor, &texture[0], static_cast<unsigned int>(fileSize))};
        MobileRT::checkSystemError("After read file.");
        ASSERT(remainingLength == 0 || remainingLength == fileSize, "File not read entirely.");
        const ::std::string fileName {filePathRaw.substr(filePathRaw.find_last_of('/') + 1, filePathRaw.size())};
        ::Components::OBJLoader::getTextureFromCache(&texturesCache_, ::std::move(texture), static_cast<long> (fileSize), fileName);
        LOG_DEBUG("Read a texture file: ", filePathRaw);
        static_cast<void>(remainingLength);
    }
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
        void *buffer{env->GetDirectBufferAddress(bufferRef)};
        float *const floatBuffer{static_cast<float *> (buffer)};
        delete[] floatBuffer;
    }
    MobileRT::checkSystemError("rtFreeNativeBuffer finish");
    return nullptr;
}
