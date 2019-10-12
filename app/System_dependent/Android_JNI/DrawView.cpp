#include "DrawView.hpp"
#include <android/bitmap.h>
#include <glm/glm.hpp>
#include <fstream>
#include <mutex>

static ::State working_{State::IDLE};
static ::std::unique_ptr<::MobileRT::Renderer> renderer_{nullptr};
static ::std::unique_ptr<::JavaVM> javaVM_{nullptr};
static ::std::unique_ptr<::std::thread> thread_{nullptr};
static ::std::mutex mutex_{};
static ::std::int32_t width_{0};
static ::std::int32_t height_{0};
static float fps_{0.0f};
static ::std::int64_t timeFrame_{0};
static ::std::int64_t timeRenderer_{0};
static ::std::int32_t numberOfLights_{0};


extern "C"
::std::int32_t JNI_OnLoad(JavaVM *const jvm, void * /*reserved*/) {
    LOG("JNI_OnLoad");
    javaVM_.reset(jvm);

    JNIEnv *jniENV{nullptr};
    {
        const ::std::int32_t result {javaVM_->GetEnv(reinterpret_cast<void **>(&jniENV), JNI_VERSION_1_6)};
        assert(result == JNI_OK);
        static_cast<void> (result);
    }
    assert(jniENV != nullptr);
    jniENV->ExceptionClear();
    return JNI_VERSION_1_6;
}

extern "C"
void JNI_OnUnload(JavaVM * /*vm*/, void * /*reserved*/) {
    LOG("JNI_OnUnload");
}

extern "C"
jobject Java_puscas_mobilertapp_DrawView_initCameraArray(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    ::MobileRT::Camera *const camera{renderer_->camera_.get()};
    const unsigned long arraySize{20};
    const jlong arrayBytes{static_cast<jlong> (arraySize) * static_cast<jlong> (sizeof(jfloat))};
    jobject directBuffer{nullptr};
    float *const floatBuffer{new float[arraySize]};

    if (floatBuffer != nullptr) {
        directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);
        if (directBuffer != nullptr) {
            int i{0};

            floatBuffer[i++] = camera->position_.x;
            floatBuffer[i++] = camera->position_.y;
            floatBuffer[i++] = camera->position_.z;
            floatBuffer[i++] = 1.0f;

            floatBuffer[i++] = camera->direction_.x;
            floatBuffer[i++] = camera->direction_.y;
            floatBuffer[i++] = camera->direction_.z;
            floatBuffer[i++] = 1.0f;

            floatBuffer[i++] = camera->up_.x;
            floatBuffer[i++] = camera->up_.y;
            floatBuffer[i++] = camera->up_.z;
            floatBuffer[i++] = 1.0f;

            floatBuffer[i++] = camera->right_.x;
            floatBuffer[i++] = camera->right_.y;
            floatBuffer[i++] = camera->right_.z;
            floatBuffer[i++] = 1.0f;

            ::Components::Perspective *perspective{
                    dynamic_cast<::Components::Perspective *>(camera)};
            ::Components::Orthographic *orthographic{
                    dynamic_cast<::Components::Orthographic *>(camera)};
            if (perspective != nullptr) {
                const float hFov{perspective->getHFov()};
                const float vFov{perspective->getVFov()};
                floatBuffer[i++] = hFov;
                floatBuffer[i++] = vFov;
                floatBuffer[i++] = 0.0f;
                floatBuffer[i++] = 0.0f;
            }
            if (orthographic != nullptr) {
                const float sizeH{orthographic->getSizeH()};
                const float sizeV{orthographic->getSizeV()};
                floatBuffer[i++] = 0.0f;
                floatBuffer[i++] = 0.0f;
                floatBuffer[i++] = sizeH;
                floatBuffer[i] = sizeV;
            }
        }
    }
    env->ExceptionClear();
    return directBuffer;
}

extern "C"
jobject Java_puscas_mobilertapp_DrawView_initVerticesArray(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    const ::std::vector<::MobileRT::Primitive<::MobileRT::Triangle>> &triangles{
            !renderer_->shader_->scene_.triangles_.empty() ?
            renderer_->shader_->scene_.triangles_ : renderer_->shader_->bvhTriangles_.primitives_};
    const unsigned long arraySize{triangles.size() * 3 * 4};
    const jlong arrayBytes{static_cast<jlong> (arraySize) * static_cast<jlong> (sizeof(jfloat))};
    jobject directBuffer{nullptr};
    if (arraySize > 0) {
        float *const floatBuffer{new float[arraySize]};
        if (floatBuffer != nullptr) {
            directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);
            if (directBuffer != nullptr) {
                int i{0};
                for (const ::MobileRT::Primitive<::MobileRT::Triangle> &triangle : triangles) {
                    const ::glm::vec4 &pointA{triangle.shape_.pointA_.x,
                                              triangle.shape_.pointA_.y,
                                              triangle.shape_.pointA_.z, 1.0f};
                    const ::glm::vec4 &pointB {pointA.x + triangle.shape_.AB_.x,
                                              pointA.y + triangle.shape_.AB_.y,
                                              pointA.z + triangle.shape_.AB_.z, 1.0f};
                    const ::glm::vec4 &pointC {pointA.x + triangle.shape_.AC_.x,
                                              pointA.y + triangle.shape_.AC_.y,
                                              pointA.z + triangle.shape_.AC_.z, 1.0f};

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
    return directBuffer;
}

extern "C"
jobject Java_puscas_mobilertapp_DrawView_initColorsArray(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    const ::std::vector<::MobileRT::Primitive<::MobileRT::Triangle>> &triangles{
            !renderer_->shader_->scene_.triangles_.empty() ?
            renderer_->shader_->scene_.triangles_ : renderer_->shader_->bvhTriangles_.primitives_};
    const unsigned long arraySize{triangles.size() * 3 * 4};
    const jlong arrayBytes{static_cast<jlong> (arraySize) * static_cast<jlong> (sizeof(jfloat))};
    jobject directBuffer{nullptr};
    if (arraySize > 0) {
        float *const floatBuffer{new float[arraySize]};
        if (floatBuffer != nullptr) {
            directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);
            if (directBuffer != nullptr) {
                int i{0};
                for (const ::MobileRT::Primitive<::MobileRT::Triangle> &triangle : triangles) {
                    const ::glm::vec3 &kD{triangle.material_.Kd_};
                    const ::glm::vec3 &kS{triangle.material_.Ks_};
                    const ::glm::vec3 &kT{triangle.material_.Kt_};
                    const ::glm::vec3 &lE{triangle.material_.Le_};
                    ::glm::vec3 color{kD};

                    color = ::glm::all(::glm::greaterThan(kS, color)) ? kS : color;
                    color = ::glm::all(::glm::greaterThan(kT, color)) ? kT : color;
                    color = ::glm::all(::glm::greaterThan(lE, color)) ? lE : color;

                    floatBuffer[i++] = color.r;
                    floatBuffer[i++] = color.g;
                    floatBuffer[i++] = color.b;
                    floatBuffer[i++] = 1.0f;

                    floatBuffer[i++] = color.r;
                    floatBuffer[i++] = color.g;
                    floatBuffer[i++] = color.b;
                    floatBuffer[i++] = 1.0f;

                    floatBuffer[i++] = color.r;
                    floatBuffer[i++] = color.g;
                    floatBuffer[i++] = color.b;
                    floatBuffer[i++] = 1.0f;
                }
            }
        }
    }
    env->ExceptionClear();
    return directBuffer;
}

static void FPS() noexcept {
    static ::std::int32_t frame{0};
    static ::std::chrono::steady_clock::time_point timebase_{};
    ++frame;
    const ::std::chrono::steady_clock::time_point time{::std::chrono::steady_clock::now()};
    const ::std::int64_t timeElapsed{
            ::std::chrono::duration_cast<std::chrono::milliseconds>(time - timebase_).count()};
    if (timeElapsed > 1000) {
        fps_ = (frame * 1000.0f) / timeElapsed;
        timebase_ = time;
        frame = 0;
    }
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_ViewText_isWorking(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    const ::std::int32_t res{static_cast<::std::int32_t> (working_)};
    env->ExceptionClear();
    return res;
}

extern "C"
void Java_puscas_mobilertapp_DrawView_stopRender(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    //TODO: Fix this race condition
    if (renderer_ != nullptr) {
        renderer_->stopRender();
    }
    working_ = State::STOPPED;
    LOG("WORKING = STOPPED");
    env->ExceptionClear();
}

static ::std::streampos fileSize(const char *filePath) {
    ::std::ifstream file{filePath, ::std::ios::binary};

    const ::std::streampos fileBegin{file.tellg()};
    file.seekg(0, ::std::ios::end);
    const ::std::streampos fileSize{file.tellg() - fileBegin};
    file.close();

    return fileSize;
}

extern "C"
jint Java_puscas_mobilertapp_DrawView_initialize(
        JNIEnv *env,
        jobject const thiz,
        jint const scene,
        jint const shader,
        jint const width,
        jint const height,
        jint const accelerator,
        jint const samplesPixel,
        jint const samplesLight,
        jstring const localObjFile,
        jstring const localMatFile
) noexcept {
    width_ = width;
    height_ = height;
    LOG("INITIALIZE");
    const jclass mainActivityClass{env->FindClass("puscas/mobilertapp/DrawView")};
    const jmethodID mainActivityMethodId{
            env->GetMethodID(mainActivityClass, "getFreeMemStatic", "(I)Z")};
    const jstring globalObjFile {static_cast<jstring>(env->NewGlobalRef(localObjFile))};
    const jstring globalMatFile {static_cast<jstring>(env->NewGlobalRef(localMatFile))};

    const ::std::int32_t res {
            [&]() noexcept -> ::std::int32_t {
        {
            const ::std::lock_guard<::std::mutex> lock {mutex_};
            renderer_ = nullptr;
        }
        const float ratio {
            ::std::max(static_cast<float>(width_) / height_, static_cast<float>(height_) / width_)};
        const float hfovFactor{width_ > height_ ? ratio : 1.0f};
        const float vfovFactor{width_ < height_ ? ratio : 1.0f};
        ::MobileRT::Scene scene_{};
        ::std::unique_ptr<MobileRT::Sampler> samplerPixel{};
        ::std::unique_ptr<MobileRT::Shader> shader_{};
        ::std::unique_ptr<MobileRT::Camera> camera{};
        ::glm::vec3 maxDist{0, 0, 0};
        switch (scene) {
            case 0: {
                const float fovX{45.0f * hfovFactor};
                const float fovY{45.0f * vfovFactor};
                camera = ::std::make_unique<Components::Perspective>(
                        ::glm::vec3 {0.0f, 0.0f, -3.4f},
                        ::glm::vec3 {0.0f, 0.0f, 1.0f},
                        ::glm::vec3 {0.0f, 1.0f, 0.0f},
                        fovX, fovY);
                scene_ = cornellBoxScene(::std::move(scene_));
                maxDist = ::glm::vec3 {1, 1, 1};
            }
                break;

            case 1: {
                const float sizeH{10.0f * hfovFactor};
                const float sizeV{10.0f * vfovFactor};
                /*camera = ::std::make_unique<Components::Perspective>(
                        ::glm::vec3 {4.0f, 4.0f, -8.0f},
                        ::glm::vec3 {4.0f, 4.0f, 4.0f},
                        ::glm::vec3 {0.0f, 1.0f, 0.0f},
                        45.0f * hfovFactor, 45.0f * vfovFactor);*/
                camera = ::std::make_unique<Components::Orthographic>(
                        ::glm::vec3 {0.0f, 1.0f, -10.0f},
                        ::glm::vec3 {0.0f, 1.0f, 7.0f},
                        ::glm::vec3 {0.0f, 1.0f, 0.0f},
                        sizeH, sizeV);
                /*camera = ::std::make_unique<Components::Perspective>(
                  ::glm::vec3 {0.0f, 0.5f, 1.0f},
                  ::glm::vec3 {0.0f, 0.0f, 7.0f},
                  ::glm::vec3 {0.0f, 1.0f, 0.0f},
                  60.0f  * hfovFactor, 60.0f * vfovFactor);*/
                scene_ = spheresScene(::std::move(scene_));
                maxDist = ::glm::vec3 {8, 8, 8};
            }
                break;

            case 2: {
                const float fovX{45.0f * hfovFactor};
                const float fovY{45.0f * vfovFactor};
                camera = ::std::make_unique<Components::Perspective>(
                        ::glm::vec3 {0.0f, 0.0f, -3.4f},
                        ::glm::vec3 {0.0f, 0.0f, 1.0f},
                        ::glm::vec3 {0.0f, 1.0f, 0.0f},
                        fovX, fovY);
                scene_ = cornellBoxScene2(::std::move(scene_));
                maxDist = ::glm::vec3 {1, 1, 1};
            }
                break;

            case 3: {
                const float fovX{60.0f * hfovFactor};
                const float fovY{60.0f * vfovFactor};
                camera = ::std::make_unique<Components::Perspective>(
                        ::glm::vec3 {0.0f, 0.5f, 1.0f},
                        ::glm::vec3 {0.0f, 0.0f, 7.0f},
                        ::glm::vec3 {0.0f, 1.0f, 0.0f},
                        fovX, fovY);
                scene_ = spheresScene2(::std::move(scene_));
                maxDist = ::glm::vec3 {8, 8, 8};
            }
                break;

            default: {
                jboolean isCopy {JNI_FALSE};
                const char *const objFileName {(env)->GetStringUTFChars(globalObjFile, &isCopy)};
                const char *const matFileName {(env)->GetStringUTFChars(globalMatFile, &isCopy)};

                assert(mainActivityMethodId != nullptr);
                {
                    const jboolean result {
                            env->CallBooleanMethod(thiz, mainActivityMethodId, 1)};
                    if (result) {
                        return -1;
                    }
                }

                ::Components::OBJLoader objLoader{objFileName, matFileName};
                {
                    const jboolean result {
                            env->CallBooleanMethod(thiz, mainActivityMethodId, 1)};
                    if (result) {
                        return -1;
                    }
                }

                const auto objSize{fileSize(objFileName) / 1048576};
                {
                    const ::std::int32_t neededMem{static_cast<::std::int32_t> (3 * objSize)};
                    const jboolean result{
                            env->CallBooleanMethod(thiz, mainActivityMethodId, neededMem)};
                    if (result) {
                        return -1;
                    }
                }
                const ::std::int32_t triangleSize{sizeof(::MobileRT::Triangle)};
                const ::std::int32_t bvhNodeSize{sizeof(::MobileRT::BVHNode)};
                const ::std::int32_t aabbSize{sizeof(::MobileRT::AABB)};
                const ::std::int32_t floatSize{sizeof(float)};
                const ::std::int32_t numberPrimitives{objLoader.process()};
                const ::std::int32_t memPrimitives{(numberPrimitives * triangleSize) / 1048576};
                const ::std::int32_t memNodes{(2 * numberPrimitives * bvhNodeSize) / 1048576};
                const ::std::int32_t memAABB{(2 * numberPrimitives * aabbSize) / 1048576};
                const ::std::int32_t memFloat{(2 * numberPrimitives * floatSize) / 1048576};
                {
                    const jboolean result {
                            env->CallBooleanMethod(thiz, mainActivityMethodId,
                                                   memPrimitives + memNodes + memAABB + memFloat)};
                    if (result) {
                        return -1;
                    }
                }

                if (!objLoader.isProcessed()) {
                    return -1;
                }
                const bool sceneBuilt{objLoader.fillScene(&scene_,
                                                          []() { return ::std::make_unique<Components::StaticHaltonSeq>(); })};
                if (!sceneBuilt) {
                    return -1;
                }
                {
                    const jboolean result {
                            env->CallBooleanMethod(thiz, mainActivityMethodId, 1)};
                    if (result) {
                        return -1;
                    }
                }

                const float fovX{45.0f * hfovFactor};
                const float fovY{45.0f * vfovFactor};
                maxDist = ::glm::vec3 {1, 1, 1};
                const ::MobileRT::Material &lightMat{::glm::vec3 {0.0f, 0.0f, 0.0f},
                                                     ::glm::vec3 {0.0f, 0.0f, 0.0f},
                                                     ::glm::vec3 {0.0f, 0.0f, 0.0f},
                                                     1.0f,
                                                     ::glm::vec3 {0.9f, 0.9f, 0.9f}};

                //cornellbox
                if (::std::strstr(objFileName, "CornellBox") != nullptr) {
                    camera = ::std::make_unique<Components::Perspective>(
                            ::glm::vec3 {0.0f, 0.7f, 3.0f},
                            ::glm::vec3 {0.0f, 0.7f, -1.0f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);

                    /*::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {0.5f, 1.58f, 0.5f},
                            ::glm::vec3 {-0.5f, 1.58f, 0.5f},
                            ::glm::vec3 {-0.5f, 1.58f, -0.5f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {0.5f, 1.58f, 0.5f},
                            ::glm::vec3 {-0.5f, 1.58f, -0.5f},
                            ::glm::vec3 {0.5f, 1.58f, -0.5f}));*/
                }

                //conference
                if (::std::strstr(objFileName, "conference") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-100.0f, 640.0f, -100.0f},
                            ::glm::vec3 {100.0f, 640.0f, -100.0f},
                            ::glm::vec3 {100.0f, 640.0f, 100.0f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-100.0f, 640.0f, -100.0f},
                            ::glm::vec3 {100.0f, 640.0f, 100.0f},
                            ::glm::vec3 {-100.0f, 640.0f, 100.0f}));
                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {460.0f, 500.0f, -1000.0f},
                            ::glm::vec3 {0.0f, 400.0f, 0.0f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                }

                //teapot
                if (::std::strstr(objFileName, "teapot") != nullptr) {
                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0f, 30.0f, -200.0f},
                            ::glm::vec3 {0.0f, 30.0f, 100.0f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-30.0f, 100.0f, -30.0f},
                            ::glm::vec3 {30.0f, 100.0f, -30.0f},
                            ::glm::vec3 {30.0f, 100.0f, 30.0f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-30.0f, 100.0f, -30.0f},
                            ::glm::vec3 {30.0f, 100.0f, 30.0f},
                            ::glm::vec3 {-30.0f, 100.0f, 30.0f}));
                }

                //dragon
                if (::std::strstr(objFileName, "dragon") != nullptr) {
                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0f, 0.0f, -1.5f},
                            ::glm::vec3 {0.0f, 0.0f, 0.0f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, 0.3f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, 0.3f},
                            ::glm::vec3 {-0.3f, 1.0f, 0.3f}));
                }

                //bedroom
                if (::std::strstr(objFileName, "bedroom") != nullptr) {
                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0f, 0.0f, -2.5f},
                            ::glm::vec3 {0.0f, 0.0f, 0.0f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, 0.3f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, 0.3f},
                            ::glm::vec3 {-0.3f, 1.0f, 0.3f}));
                }

                //breakfast_room
                if (::std::strstr(objFileName, "breakfast_room")) {
                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0f, 0.0f, -5.0f},
                            ::glm::vec3 {0.0f, 0.0f, 0.0f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, 0.3f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, 0.3f},
                            ::glm::vec3 {-0.3f, 1.0f, 0.3f}));
                }

                //buddha
                if (::std::strstr(objFileName, "buddha") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, 0.3f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, 0.3f},
                            ::glm::vec3 {-0.3f, 1.0f, 0.3f}));

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0f, 0.0f, -2.5f},
                            ::glm::vec3 {0.0f, 0.0f, 0.0f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                }

                //erato
                if (::std::strstr(objFileName, "erato") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, 0.3f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, 0.3f},
                            ::glm::vec3 {-0.3f, 1.0f, 0.3f}));

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0f, 0.0f, -2.5f},
                            ::glm::vec3 {-11442.307617f, -12999.129883, -12729.150391},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                }

                //gallery
                if (::std::strstr(objFileName, "gallery") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, 0.3f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.3f, 1.0f, -0.3f},
                            ::glm::vec3 {0.3f, 1.0f, 0.3f},
                            ::glm::vec3 {-0.3f, 1.0f, 0.3f}));

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {0.0f, 0.0f, -2.5f},
                            ::glm::vec3 {0.0f, 0.0f, 0.0f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                }

                //Porsche
                if (::std::strstr(objFileName, "Porsche") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-1.0f, 2.1f, 1.0f},
                            ::glm::vec3 {1.0f, 2.1f, -1.0f},
                            ::glm::vec3 {1.0f, 2.1f, 1.0f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-1.0f, 2.1f, 1.0f},
                            ::glm::vec3 {-1.0f, 2.1f, -1.0f},
                            ::glm::vec3 {1.0f, 2.1f, -1.0f}));

                    const ::MobileRT::Material planeMaterialBack{::glm::vec3 {0.7f, 0.7f, 0.7f}};
                    const ::glm::vec3 planePointDown{0.0f, -1.0f, 0.0f};
                    const ::glm::vec3 planeNormalDown{0.0f, 1.0f, 0.0f};
                    const ::MobileRT::Plane planeDown{planePointDown, planeNormalDown};
                    const ::MobileRT::Primitive<::MobileRT::Plane> planePrimitiveDown{planeDown,
                                                                                      planeMaterialBack};
                    scene_.planes_.emplace_back(planePrimitiveDown);

                    const ::glm::vec3 planePointUp{0.0f, 2.2f, 0.0f};
                    const ::glm::vec3 planeNormalUp{0.0f, -1.0f, 0.0f};
                    const ::MobileRT::Plane planeUp{planePointUp, planeNormalUp};
                    const ::MobileRT::Primitive<::MobileRT::Plane> planePrimitiveUp{planeUp,
                                                                                    planeMaterialBack};
                    scene_.planes_.emplace_back(planePrimitiveUp);

                    const ::MobileRT::Material planeMaterialLeft{::glm::vec3 {0.9f, 0.0f, 0.0f}};
                    const ::glm::vec3 planePointLeft{-4.1f, 0.0f, 0.0f};
                    const ::glm::vec3 planeNormalLeft{1.0f, 0.0f, 0.0f};
                    const ::MobileRT::Plane planeLeft{planePointLeft, planeNormalLeft};
                    const ::MobileRT::Primitive<::MobileRT::Plane> planePrimitiveLeft{planeLeft,
                                                                                      planeMaterialLeft};
                    scene_.planes_.emplace_back(planePrimitiveLeft);

                    const ::MobileRT::Material planeMaterialRight{::glm::vec3 {0.0f, 0.0f, 0.9f}};
                    const ::glm::vec3 planePointRight{1.1f, 0.0f, 0.0f};
                    const ::glm::vec3 planeNormalRight{-1.0f, 0.0f, 0.0f};
                    const ::MobileRT::Plane planeRight{planePointRight, planeNormalRight};
                    const ::MobileRT::Primitive<::MobileRT::Plane> planePrimitiveRight{planeRight,
                                                                                       planeMaterialRight};
                    scene_.planes_.emplace_back(planePrimitiveRight);

                    const ::glm::vec3 planePointBack{0.0f, 0.0f, -4.6f};
                    const ::glm::vec3 planeNormalBack{0.0f, 0.0f, 1.0f};
                    const ::MobileRT::Plane planeBack{planePointBack, planeNormalBack};
                    const ::MobileRT::Primitive<::MobileRT::Plane> planePrimitiveBack{planeBack,
                                                                                      planeMaterialBack};
                    scene_.planes_.emplace_back(planePrimitiveBack);

                    const ::MobileRT::Material planeMaterialForward{::glm::vec3 {0.0f, 0.9f, 0.9f}};
                    const ::glm::vec3 planePointForward{0.0f, 0.0f, 2.3f};
                    const ::glm::vec3 planeNormalForward{0.0f, 0.0f, -1.0f};
                    const ::MobileRT::Plane planeForward{planePointForward, planeNormalForward};
                    const ::MobileRT::Primitive<::MobileRT::Plane> planePrimitiveForward{
                            planeForward, planeMaterialForward};
                    scene_.planes_.emplace_back(planePrimitiveForward);

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {-4.0f, 2.0f, -4.5f},
                            ::glm::vec3 {0.0f, 0.0f, 0.0f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                }

                //Power Plant
                if (::std::strstr(objFileName, "powerplant") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-1.0f, 1.5f, 1.0f},
                            ::glm::vec3 {1.0f, 1.5f, -1.0f},
                            ::glm::vec3 {1.0f, 1.5f, 1.0f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-1.0f, 1.5f, 1.0f},
                            ::glm::vec3 {-1.0f, 1.5f, -1.0f},
                            ::glm::vec3 {1.0f, 1.5f, -1.0f}));

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {-1.0f, -1.0f, -1.0f},
                            ::glm::vec3 {0.005265f, 0.801842f, -0.150495f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                }

                //Road Bike
                if (::std::strstr(objFileName, "roadBike") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.5f, 1.5f, 0.5f},
                            ::glm::vec3 {0.5f, 1.5f, -0.5f},
                            ::glm::vec3 {0.5f, 1.5f, 0.5f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.5f, 1.5f, 0.5f},
                            ::glm::vec3 {-0.5f, 1.5f, -0.5f},
                            ::glm::vec3 {0.5f, 1.5f, -0.5f}));

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {-2.0f, 0.0f, -2.0f},
                            ::glm::vec3 {0.0f, 0.5f, 0.0f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                }

                //San Miguel
                if (::std::strstr(objFileName, "San_Miguel") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.5f, 1.5f, 0.5f},
                            ::glm::vec3 {0.5f, 1.5f, -0.5f},
                            ::glm::vec3 {0.5f, 1.5f, 0.5f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.5f, 1.5f, 0.5f},
                            ::glm::vec3 {-0.5f, 1.5f, -0.5f},
                            ::glm::vec3 {0.5f, 1.5f, -0.5f}));

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {-19.3901f, 0.798561f, 6.01737f},
                            ::glm::vec3 {-49.3901f, 0.798561f, 6.01737f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                }

                //Sports Car
                if (::std::strstr(objFileName, "sportsCar") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-0.5f, 1.5f, 0.5f},
                            ::glm::vec3 {0.5f, 1.5f, -0.5f},
                            ::glm::vec3 {0.5f, 1.5f, 0.5f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-0.5f, 1.5f, 0.5f},
                            ::glm::vec3 {-0.5f, 1.5f, -0.5f},
                            ::glm::vec3 {0.5f, 1.5f, -0.5f}));

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {-6.0f, 3.798561f, -6.0f},
                            ::glm::vec3 {0.5110f, 0.8459f, -0.7924f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                }

                //Elvira_Holiday
                if (::std::strstr(objFileName, "Elvira_Holiday") != nullptr) {
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint1),
                            ::glm::vec3 {-50.5f, 41.5f, 50.5f},
                            ::glm::vec3 {50.5f, 41.5f, -50.5f},
                            ::glm::vec3 {50.5f, 41.5f, 50.5f}));
                    ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                            ::std::make_unique<Components::StaticHaltonSeq>()};
                    scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                            lightMat,
                            ::std::move(samplerPoint2),
                            ::glm::vec3 {-50.5f, 41.5f, 50.5f},
                            ::glm::vec3 {-50.5f, 41.5f, -50.5f},
                            ::glm::vec3 {50.5f, 41.5f, -50.5f}));

                    camera = ::std::make_unique<::Components::Perspective>(
                            ::glm::vec3 {11.0754f - 50.0f, 22.8368f + 10.0f, -0.2654f - 50.0f},
                            ::glm::vec3 {11.0754f, 22.8368f, -0.2654f},
                            ::glm::vec3 {0.0f, 1.0f, 0.0f},
                            fovX, fovY);
                }

                env->ReleaseStringUTFChars(globalObjFile, objFileName);
                env->ReleaseStringUTFChars(globalMatFile, matFileName);
            }
                break;
        }
        if (samplesPixel > 1) {
            samplerPixel = ::std::make_unique<Components::StaticHaltonSeq>();
        } else {
            samplerPixel = ::std::make_unique<Components::Constant>(0.5f);
        }
        switch (shader) {
            case 1: {
                shader_ = ::std::make_unique<Components::Whitted>(::std::move(scene_), samplesLight,
                                                                ::MobileRT::Shader::Accelerator(
                                                                        accelerator));
                break;
            }

            case 2: {
                ::std::unique_ptr<MobileRT::Sampler> samplerRussianRoulette{
                        ::std::make_unique<Components::StaticHaltonSeq>()};

                shader_ = ::std::make_unique<Components::PathTracer>(
                        ::std::move(scene_), ::std::move(samplerRussianRoulette), samplesLight,
                        ::MobileRT::Shader::Accelerator(accelerator));
                break;
            }

            case 3: {
                shader_ = ::std::make_unique<Components::DepthMap>(::std::move(scene_), maxDist,
                                                                 ::MobileRT::Shader::Accelerator(
                                                                         accelerator));
                break;
            }

            case 4: {
                shader_ = ::std::make_unique<Components::DiffuseMaterial>(::std::move(scene_),
                                                                        ::MobileRT::Shader::Accelerator(
                                                                                accelerator));
                break;
            }

            default: {
                shader_ = ::std::make_unique<Components::NoShadows>(::std::move(scene_), samplesLight,
                                                                  ::MobileRT::Shader::Accelerator(
                                                                          accelerator));
                break;
            }
        }
                const ::std::int32_t triangles {
                static_cast<int32_t> (shader_->scene_.triangles_.size())};
                const ::std::int32_t spheres {
                static_cast<int32_t> (shader_->scene_.spheres_.size())};
                const ::std::int32_t planes {
                        static_cast<::std::int32_t> (shader_->scene_.planes_.size())};
                numberOfLights_ = static_cast<::std::int32_t> (shader_->scene_.lights_.size());
                const ::std::int32_t nPrimitives {triangles + spheres + planes};
        {
            const ::std::lock_guard<::std::mutex> lock {mutex_};
            const ::std::chrono::time_point<::std::chrono::system_clock> start {
                    ::std::chrono::system_clock::now()};
            renderer_ = ::std::make_unique<::MobileRT::Renderer>(
                    ::std::move(shader_), ::std::move(camera), ::std::move(samplerPixel),
                    static_cast<::std::uint32_t>(width_), static_cast<::std::uint32_t>(height_),
                    static_cast<::std::uint32_t>(samplesPixel));
            const ::std::chrono::time_point<::std::chrono::system_clock> end {
                    ::std::chrono::system_clock::now()};
            timeRenderer_ = ::std::chrono::duration_cast<std::chrono::milliseconds>(
                    end - start).count();
            LOG("TIME CONSTRUCTION RENDERER = ", timeRenderer_, "ms");
        }
        /*LOG("TRIANGLES = ", triangles);
        LOG("SPHERES = ", spheres);
        LOG("PLANES = ", planes);
        LOG("LIGHTS = ", numberOfLights_);*/
        return nPrimitives;
    }()};


    env->ExceptionClear();
    LOG("PRIMITIVES = ", res);

    {
      const jboolean result {
              env->CallBooleanMethod(thiz, mainActivityMethodId, 1)};
      if (result) {
          return -1;
      }
    }

    return res;
}

extern "C"
void Java_puscas_mobilertapp_DrawView_finishRender(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    //TODO: Fix this race condition
    if (renderer_ != nullptr) {
        renderer_->stopRender();
    }
    if (thread_ != nullptr) {
        {
            const ::std::lock_guard<::std::mutex> lock {mutex_};
            renderer_.reset();
            renderer_ = nullptr;
            thread_.reset();
            thread_ = nullptr;
            LOG("DELETED RENDERER");
        }
    }
    working_ = State::IDLE;
    LOG("WORKING = IDLE");
    timeFrame_ = 0;
    fps_ = 0.0f;
    timeRenderer_ = 0;
    env->ExceptionClear();
}

extern "C"
void Java_puscas_mobilertapp_DrawView_renderIntoBitmap(
        JNIEnv *const env,
        jobject /*thiz*/,
        jobject localBitmap,
        jint const nThreads,
        jboolean const async
) noexcept {
    jobject globalBitmap{static_cast<jobject>(env->NewGlobalRef(localBitmap))};

    working_ = State::BUSY;
    LOG("WORKING = BUSY");

    auto lambda {
        [=]() noexcept -> void {
        assert(env != nullptr);
        const ::std::int32_t jniError {
                javaVM_->GetEnv(reinterpret_cast<void **>(const_cast<JNIEnv **>(&env)),
                                JNI_VERSION_1_6)};

        assert(jniError == JNI_OK || jniError == JNI_EDETACHED);
        {
            const ::std::int32_t result {javaVM_->AttachCurrentThread(const_cast<JNIEnv **>(&env), nullptr)};
            assert(result == JNI_OK);
            static_cast<void>(result);
        }
        const ::std::int32_t jniThread {jniError == JNI_EDETACHED? JNI_EDETACHED : JNI_OK};

            ::std::uint32_t *dstPixels{nullptr};
        {
            const ::std::int32_t ret {AndroidBitmap_lockPixels(env, globalBitmap, reinterpret_cast<void **>(&dstPixels))};
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

            const ::std::uint32_t stride{info.stride};
        ::std::int32_t rep {1};
        do {
            LOG("STARTING RENDERING");
            LOG("nThreads = ", nThreads);
            {
                const ::std::lock_guard<::std::mutex> lock {mutex_};
                const ::std::chrono::steady_clock::time_point start{
                        ::std::chrono::steady_clock::now()};
                if (renderer_ != nullptr) {
                    renderer_->renderFrame(dstPixels, nThreads, stride);
                }
                const ::std::chrono::steady_clock::time_point end{
                        ::std::chrono::steady_clock::now()};
                timeFrame_ = ::std::chrono::duration_cast<::std::chrono::milliseconds>(
                        end - start).count();
            }
            LOG("FINISHED RENDERING");
            FPS();
            rep--;
        } while (working_ != State::STOPPED && rep > 0);
        if (working_ != State::STOPPED) {
            working_ = State::FINISHED;
            LOG("WORKING = FINISHED");
        }
        {
            const ::std::int32_t result {AndroidBitmap_unlockPixels(env, globalBitmap)};
            assert(result == JNI_OK);
            static_cast<void> (result);
        }

        env->DeleteGlobalRef(globalBitmap);
        {
            const ::std::int32_t result {javaVM_->GetEnv(reinterpret_cast<void **>(const_cast<JNIEnv **>(&env)),
                                              JNI_VERSION_1_6)};
            assert(result == JNI_OK);
            static_cast<void> (result);
        }
            env->ExceptionClear();
        if (jniThread == JNI_EDETACHED) {
            const ::std::int32_t result {javaVM_->DetachCurrentThread()};
            assert(result == JNI_OK);
            static_cast<void> (result);
        }
    }};

    if (async) {
        thread_ = ::std::make_unique<::std::thread>(lambda);
        thread_->detach();
    } else {
        lambda();
    }
    env->ExceptionClear();
}

extern "C"
float Java_puscas_mobilertapp_ViewText_getFPS(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    env->ExceptionClear();
    return fps_;
}

extern "C"
jlong Java_puscas_mobilertapp_ViewText_getTimeRenderer(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    env->ExceptionClear();
    return timeRenderer_;
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_ViewText_getSample(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    ::std::uint32_t sample{0};
    {
        //const ::std::lock_guard<::std::mutex> lock {mutex_};
        //TODO: Fix this race condition
        if (renderer_ != nullptr) {
            sample = renderer_->getSample();
        }
    }
    env->ExceptionClear();
    const ::std::int32_t res{static_cast<::std::int32_t> (sample)};
    return res;
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_DrawView_resize(
        JNIEnv *env,
        jobject /*thiz*/,
        jint const size
) noexcept {
    const ::std::int32_t res{::MobileRT::roundDownToMultipleOf(size,
                                                               static_cast<::std::int32_t>(::std::sqrt(
                                                                       MobileRT::NumberOfBlocks)))};
    env->ExceptionClear();
    return res;
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_DrawView_getNumberOfLights(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    env->ExceptionClear();
    return numberOfLights_;
}

extern "C"
jobject Java_puscas_mobilertapp_DrawView_freeNativeBuffer(
        JNIEnv *env,
        jobject /*thiz*/,
        jobject bufferRef
) noexcept {
    if (bufferRef != nullptr) {
        void *buffer{env->GetDirectBufferAddress(bufferRef)};
        float *const floatBuffer{static_cast<float *>(buffer)};
        delete[] floatBuffer;
    }
    return nullptr;
}
