#include "DrawView.hpp"
#include <android/bitmap.h>
#include <glm/glm.hpp>
#include <fstream>
#include <mutex>

static ::std::atomic<::State> state_{State::IDLE};
static ::std::unique_ptr<::MobileRT::Renderer> renderer_{nullptr};
static ::std::unique_ptr<::JavaVM> javaVM_{nullptr};
static ::std::unique_ptr<::std::thread> thread_{nullptr};
static ::std::mutex mutex_{};
static ::std::int32_t width_{0};
static ::std::int32_t height_{0};
static float fps_{0.0F};
static ::std::int64_t timeRenderer_{0};
static ::std::int32_t numberOfLights_{0};
static ::std::condition_variable rendered_ {};
static ::std::atomic<bool> notified_ {false};


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
jobject Java_puscas_mobilertapp_MainRenderer_RTInitCameraArray(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    jobject directBuffer{nullptr};
    {
        const ::std::lock_guard<::std::mutex> lock {mutex_};
        if (renderer_ != nullptr) {
            ::MobileRT::Camera *const camera{renderer_->camera_.get()};
            const unsigned long arraySize{20};
            const jlong arrayBytes{static_cast<jlong> (arraySize) * static_cast<jlong> (sizeof(jfloat))};
            float *const floatBuffer{new float[arraySize]};

            if (floatBuffer != nullptr) {
                directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);
                if (directBuffer != nullptr) {
                    int i{0};

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

                    ::Components::Perspective *perspective{
                            dynamic_cast<::Components::Perspective *>(camera)};
                    ::Components::Orthographic *orthographic{
                            dynamic_cast<::Components::Orthographic *>(camera)};
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
    return directBuffer;
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTInitVerticesArray(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    jobject directBuffer{nullptr};
    {
        const ::std::lock_guard<::std::mutex> lock{mutex_};
        if (renderer_ != nullptr) {
            const ::std::vector<::MobileRT::Primitive<::MobileRT::Triangle>> &triangles{
                    !renderer_->shader_->scene_.triangles_.empty() ?
                    renderer_->shader_->scene_.triangles_ : renderer_->shader_->bvhTriangles_.primitives_};
            const unsigned long arraySize{triangles.size() * 3 * 4};
            const jlong arrayBytes{static_cast<jlong> (arraySize) * static_cast<jlong> (sizeof(jfloat))};
            if (arraySize > 0) {
                float *const floatBuffer{new float[arraySize]};
                if (floatBuffer != nullptr) {
                    directBuffer = env->NewDirectByteBuffer(floatBuffer, arrayBytes);
                    if (directBuffer != nullptr) {
                        int i{0};
                        for (const ::MobileRT::Primitive<::MobileRT::Triangle> &triangle : triangles) {
                            const ::glm::vec4 &pointA{triangle.shape_.pointA_.x,
                                                      triangle.shape_.pointA_.y,
                                                      triangle.shape_.pointA_.z, 1.0F};
                            const ::glm::vec4 &pointB{pointA.x + triangle.shape_.AB_.x,
                                                      pointA.y + triangle.shape_.AB_.y,
                                                      pointA.z + triangle.shape_.AB_.z, 1.0F};
                            const ::glm::vec4 &pointC{pointA.x + triangle.shape_.AC_.x,
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
    jobject directBuffer{nullptr};
    {
        const ::std::lock_guard<::std::mutex> lock{mutex_};
        if (renderer_ != nullptr) {
            const ::std::vector<::MobileRT::Primitive<::MobileRT::Triangle>> &triangles{
                    !renderer_->shader_->scene_.triangles_.empty() ?
                    renderer_->shader_->scene_.triangles_ : renderer_->shader_->bvhTriangles_.primitives_};
            const unsigned long arraySize{triangles.size() * 3 * 4};
            const jlong arrayBytes{static_cast<jlong> (arraySize) * static_cast<jlong> (sizeof(jfloat))};
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

static void FPS() noexcept {
    static ::std::int32_t frame{0};
    static ::std::chrono::steady_clock::time_point timebase_{};
    ++frame;
    const ::std::chrono::steady_clock::time_point time{::std::chrono::steady_clock::now()};
    const ::std::int64_t timeElapsed{
            ::std::chrono::duration_cast<std::chrono::milliseconds>(time - timebase_).count()};
    if (timeElapsed > 1000) {
        fps_ = (frame * 1000.0F) / timeElapsed;
        timebase_ = time;
        frame = 0;
    }
}

extern "C"
void Java_puscas_mobilertapp_DrawView_RTStartRender(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    notified_ = false;
    state_ = State::BUSY;
    LOG("WORKING = BUSY");
    env->ExceptionClear();
}

extern "C"
void Java_puscas_mobilertapp_DrawView_RTStopRender(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    //TODO: Fix this race condition
    while (!notified_ && renderer_ != nullptr) {
        state_ = State::STOPPED;
        LOG("WORKING = STOPPED");
        if (renderer_ != nullptr) {
            renderer_->stopRender();
        }

        //::std::unique_lock<std::mutex> lock {mutex_};
        //rendered_.wait(lock);
    }
    state_ = State::STOPPED;
    LOG("WORKING = STOPPED");
    {
        ::std::unique_lock<std::mutex> lock {mutex_};
        while (!notified_){
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
    ::std::ifstream file{filePath, ::std::ios::binary};

    const ::std::streampos fileBegin{file.tellg()};
    file.seekg(0, ::std::ios::end);
    const ::std::streampos fileSize{file.tellg() - fileBegin};
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
        jstring localMatFile
) noexcept {
    width_ = width;
    height_ = height;
    LOG("INITIALIZE");
    const jclass rendererClass{env->FindClass("puscas/mobilertapp/MainRenderer")};
    const jmethodID isLowMemoryMethodId{
            env->GetMethodID(rendererClass, "isLowMemory", "(I)Z")};
    const jstring globalObjFile {static_cast<jstring>(env->NewGlobalRef(localObjFile))};
    const jstring globalMatFile {static_cast<jstring>(env->NewGlobalRef(localMatFile))};

    const ::std::int32_t res {
            [&]() noexcept -> ::std::int32_t {
        {
            const ::std::lock_guard<::std::mutex> lock {mutex_};
            renderer_ = nullptr;
            const float ratio {
                ::std::max(static_cast<float>(width_) / height_, static_cast<float>(height_) / width_)};
            const float hfovFactor{width_ > height_ ? ratio : 1.0F};
            const float vfovFactor{width_ < height_ ? ratio : 1.0F};
            ::MobileRT::Scene scene_{};
            ::std::unique_ptr<MobileRT::Sampler> samplerPixel{};
            ::std::unique_ptr<MobileRT::Shader> shader_{};
            ::std::unique_ptr<MobileRT::Camera> camera{};
            ::glm::vec3 maxDist{0, 0, 0};
            switch (scene) {
                case 0: {
                    const float fovX{45.0F * hfovFactor};
                    const float fovY{45.0F * vfovFactor};
                    camera = ::std::make_unique<Components::Perspective>(
                            ::glm::vec3 {0.0F, 0.0F, -3.4F},
                            ::glm::vec3 {0.0F, 0.0F, 1.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);
                    scene_ = cornellBoxScene(::std::move(scene_));
                    maxDist = ::glm::vec3 {1, 1, 1};
                }
                    break;

                case 1: {
                    const float sizeH{10.0F * hfovFactor};
                    const float sizeV{10.0F * vfovFactor};
                    /*camera = ::std::make_unique<Components::Perspective>(
                            ::glm::vec3 {4.0F, 4.0F, -8.0F},
                            ::glm::vec3 {4.0F, 4.0F, 4.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            45.0F * hfovFactor, 45.0F * vfovFactor);*/
                    camera = ::std::make_unique<Components::Orthographic>(
                            ::glm::vec3 {0.0F, 1.0F, -10.0F},
                            ::glm::vec3 {0.0F, 1.0F, 7.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            sizeH, sizeV);
                    /*camera = ::std::make_unique<Components::Perspective>(
                      ::glm::vec3 {0.0F, 0.5F, 1.0F},
                      ::glm::vec3 {0.0F, 0.0F, 7.0F},
                      ::glm::vec3 {0.0F, 1.0F, 0.0F},
                      60.0F  * hfovFactor, 60.0F * vfovFactor);*/
                    scene_ = spheresScene(::std::move(scene_));
                    maxDist = ::glm::vec3 {8, 8, 8};
                }
                    break;

                case 2: {
                    const float fovX{45.0F * hfovFactor};
                    const float fovY{45.0F * vfovFactor};
                    camera = ::std::make_unique<Components::Perspective>(
                            ::glm::vec3 {0.0F, 0.0F, -3.4F},
                            ::glm::vec3 {0.0F, 0.0F, 1.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);
                    scene_ = cornellBoxScene2(::std::move(scene_));
                    maxDist = ::glm::vec3 {1, 1, 1};
                }
                    break;

                case 3: {
                    const float fovX{60.0F * hfovFactor};
                    const float fovY{60.0F * vfovFactor};
                    camera = ::std::make_unique<Components::Perspective>(
                            ::glm::vec3 {0.0F, 0.5F, 1.0F},
                            ::glm::vec3 {0.0F, 0.0F, 7.0F},
                            ::glm::vec3 {0.0F, 1.0F, 0.0F},
                            fovX, fovY);
                    scene_ = spheresScene2(::std::move(scene_));
                    maxDist = ::glm::vec3 {8, 8, 8};
                }
                    break;

                default: {
                    jboolean isCopy {JNI_FALSE};
                    const char *const objFileName {(env)->GetStringUTFChars(globalObjFile, &isCopy)};
                    const char *const matFileName {(env)->GetStringUTFChars(globalMatFile, &isCopy)};

                    assert(isLowMemoryMethodId != nullptr);
                    assert(objFileName != nullptr);
                    assert(matFileName != nullptr);
                    {
                        const jboolean result {
                                env->CallBooleanMethod(thiz, isLowMemoryMethodId, 1)};
                        if (result) {
                            return -1;
                        }
                    }

                    ::Components::OBJLoader objLoader{objFileName, matFileName};
                    {
                        const jboolean result {
                                env->CallBooleanMethod(thiz, isLowMemoryMethodId, 1)};
                        if (result) {
                            return -1;
                        }
                    }

                    const auto objSize{fileSize(objFileName)};
                    {
                        const ::std::int32_t neededMem{static_cast<::std::int32_t> (3 * (objSize / 1048576))};
                        const jboolean result{
                                env->CallBooleanMethod(thiz, isLowMemoryMethodId, neededMem)};
                        if (result) {
                            notified_ = true;
                            return -1;
                        }
                        if (objSize <= 0) {
                            notified_ = true;
                            return -2;
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
                                env->CallBooleanMethod(thiz, isLowMemoryMethodId,
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
                                env->CallBooleanMethod(thiz, isLowMemoryMethodId, 1)};
                        if (result) {
                            return -1;
                        }
                    }

                    const float fovX{45.0F * hfovFactor};
                    const float fovY{45.0F * vfovFactor};
                    maxDist = ::glm::vec3 {1, 1, 1};
                    const ::MobileRT::Material &lightMat{::glm::vec3 {0.0F, 0.0F, 0.0F},
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

                        /*::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
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
                                ::glm::vec3 {0.5F, 1.58F, -0.5F}));*/
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
                    if (::std::strstr(objFileName, "breakfast_room")) {
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
                                ::glm::vec3 {-11442.307617F, -12999.129883, -12729.150391},
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
                                ::glm::vec3 {-1.0F, 2.1F, 1.0F},
                                ::glm::vec3 {1.0F, 2.1F, -1.0F},
                                ::glm::vec3 {1.0F, 2.1F, 1.0F}));
                        ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                                ::std::make_unique<Components::StaticHaltonSeq>()};
                        scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                                lightMat,
                                ::std::move(samplerPoint2),
                                ::glm::vec3 {-1.0F, 2.1F, 1.0F},
                                ::glm::vec3 {-1.0F, 2.1F, -1.0F},
                                ::glm::vec3 {1.0F, 2.1F, -1.0F}));

                        const ::MobileRT::Material planeMaterialBack{::glm::vec3 {0.7F, 0.7F, 0.7F}};
                        const ::glm::vec3 planePointDown{0.0F, -1.0F, 0.0F};
                        const ::glm::vec3 planeNormalDown{0.0F, 1.0F, 0.0F};
                        const ::MobileRT::Plane planeDown{planePointDown, planeNormalDown};
                        const ::MobileRT::Primitive<::MobileRT::Plane> planePrimitiveDown{planeDown,
                                                                                          planeMaterialBack};
                        scene_.planes_.emplace_back(planePrimitiveDown);

                        const ::glm::vec3 planePointUp{0.0F, 2.2F, 0.0F};
                        const ::glm::vec3 planeNormalUp{0.0F, -1.0F, 0.0F};
                        const ::MobileRT::Plane planeUp{planePointUp, planeNormalUp};
                        const ::MobileRT::Primitive<::MobileRT::Plane> planePrimitiveUp{planeUp,
                                                                                        planeMaterialBack};
                        scene_.planes_.emplace_back(planePrimitiveUp);

                        const ::MobileRT::Material planeMaterialLeft{::glm::vec3 {0.9F, 0.0F, 0.0F}};
                        const ::glm::vec3 planePointLeft{-4.1F, 0.0F, 0.0F};
                        const ::glm::vec3 planeNormalLeft{1.0F, 0.0F, 0.0F};
                        const ::MobileRT::Plane planeLeft{planePointLeft, planeNormalLeft};
                        const ::MobileRT::Primitive<::MobileRT::Plane> planePrimitiveLeft{planeLeft,
                                                                                          planeMaterialLeft};
                        scene_.planes_.emplace_back(planePrimitiveLeft);

                        const ::MobileRT::Material planeMaterialRight{::glm::vec3 {0.0F, 0.0F, 0.9F}};
                        const ::glm::vec3 planePointRight{1.1F, 0.0F, 0.0F};
                        const ::glm::vec3 planeNormalRight{-1.0F, 0.0F, 0.0F};
                        const ::MobileRT::Plane planeRight{planePointRight, planeNormalRight};
                        const ::MobileRT::Primitive<::MobileRT::Plane> planePrimitiveRight{planeRight,
                                                                                           planeMaterialRight};
                        scene_.planes_.emplace_back(planePrimitiveRight);

                        const ::glm::vec3 planePointBack{0.0F, 0.0F, -4.6F};
                        const ::glm::vec3 planeNormalBack{0.0F, 0.0F, 1.0F};
                        const ::MobileRT::Plane planeBack{planePointBack, planeNormalBack};
                        const ::MobileRT::Primitive<::MobileRT::Plane> planePrimitiveBack{planeBack,
                                                                                          planeMaterialBack};
                        scene_.planes_.emplace_back(planePrimitiveBack);

                        const ::MobileRT::Material planeMaterialForward{::glm::vec3 {0.0F, 0.9F, 0.9F}};
                        const ::glm::vec3 planePointForward{0.0F, 0.0F, 2.3F};
                        const ::glm::vec3 planeNormalForward{0.0F, 0.0F, -1.0F};
                        const ::MobileRT::Plane planeForward{planePointForward, planeNormalForward};
                        const ::MobileRT::Primitive<::MobileRT::Plane> planePrimitiveForward{
                                planeForward, planeMaterialForward};
                        scene_.planes_.emplace_back(planePrimitiveForward);

                        camera = ::std::make_unique<::Components::Perspective>(
                                ::glm::vec3 {-4.0F, 2.0F, -4.5F},
                                ::glm::vec3 {0.0F, 0.0F, 0.0F},
                                ::glm::vec3 {0.0F, 1.0F, 0.0F},
                                fovX, fovY);
                    }

                    //Power Plant
                    if (::std::strstr(objFileName, "powerplant") != nullptr) {
                        ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                                ::std::make_unique<Components::StaticHaltonSeq>()};
                        scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                                lightMat,
                                ::std::move(samplerPoint1),
                                ::glm::vec3 {-1.0F, 1.5F, 1.0F},
                                ::glm::vec3 {1.0F, 1.5F, -1.0F},
                                ::glm::vec3 {1.0F, 1.5F, 1.0F}));
                        ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                                ::std::make_unique<Components::StaticHaltonSeq>()};
                        scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                                lightMat,
                                ::std::move(samplerPoint2),
                                ::glm::vec3 {-1.0F, 1.5F, 1.0F},
                                ::glm::vec3 {-1.0F, 1.5F, -1.0F},
                                ::glm::vec3 {1.0F, 1.5F, -1.0F}));

                        camera = ::std::make_unique<::Components::Perspective>(
                                ::glm::vec3 {-1.0F, -1.0F, -1.0F},
                                ::glm::vec3 {0.005265F, 0.801842F, -0.150495F},
                                ::glm::vec3 {0.0F, 1.0F, 0.0F},
                                fovX, fovY);
                    }

                    //Road Bike
                    if (::std::strstr(objFileName, "roadBike") != nullptr) {
                        ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                                ::std::make_unique<Components::StaticHaltonSeq>()};
                        scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                                lightMat,
                                ::std::move(samplerPoint1),
                                ::glm::vec3 {-0.5F, 1.5F, 0.5F},
                                ::glm::vec3 {0.5F, 1.5F, -0.5F},
                                ::glm::vec3 {0.5F, 1.5F, 0.5F}));
                        ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                                ::std::make_unique<Components::StaticHaltonSeq>()};
                        scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                                lightMat,
                                ::std::move(samplerPoint2),
                                ::glm::vec3 {-0.5F, 1.5F, 0.5F},
                                ::glm::vec3 {-0.5F, 1.5F, -0.5F},
                                ::glm::vec3 {0.5F, 1.5F, -0.5F}));

                        camera = ::std::make_unique<::Components::Perspective>(
                                ::glm::vec3 {-2.0F, 0.0F, -2.0F},
                                ::glm::vec3 {0.0F, 0.5F, 0.0F},
                                ::glm::vec3 {0.0F, 1.0F, 0.0F},
                                fovX, fovY);
                    }

                    //San Miguel
                    if (::std::strstr(objFileName, "San_Miguel") != nullptr) {
                        ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                                ::std::make_unique<Components::StaticHaltonSeq>()};
                        scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                                lightMat,
                                ::std::move(samplerPoint1),
                                ::glm::vec3 {-0.5F, 1.5F, 0.5F},
                                ::glm::vec3 {0.5F, 1.5F, -0.5F},
                                ::glm::vec3 {0.5F, 1.5F, 0.5F}));
                        ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                                ::std::make_unique<Components::StaticHaltonSeq>()};
                        scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                                lightMat,
                                ::std::move(samplerPoint2),
                                ::glm::vec3 {-0.5F, 1.5F, 0.5F},
                                ::glm::vec3 {-0.5F, 1.5F, -0.5F},
                                ::glm::vec3 {0.5F, 1.5F, -0.5F}));

                        camera = ::std::make_unique<::Components::Perspective>(
                                ::glm::vec3 {-19.3901F, 0.798561F, 6.01737F},
                                ::glm::vec3 {-49.3901F, 0.798561F, 6.01737F},
                                ::glm::vec3 {0.0F, 1.0F, 0.0F},
                                fovX, fovY);
                    }

                    //Sports Car
                    if (::std::strstr(objFileName, "sportsCar") != nullptr) {
                        ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                                ::std::make_unique<Components::StaticHaltonSeq>()};
                        scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                                lightMat,
                                ::std::move(samplerPoint1),
                                ::glm::vec3 {-0.5F, 1.5F, 0.5F},
                                ::glm::vec3 {0.5F, 1.5F, -0.5F},
                                ::glm::vec3 {0.5F, 1.5F, 0.5F}));
                        ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                                ::std::make_unique<Components::StaticHaltonSeq>()};
                        scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                                lightMat,
                                ::std::move(samplerPoint2),
                                ::glm::vec3 {-0.5F, 1.5F, 0.5F},
                                ::glm::vec3 {-0.5F, 1.5F, -0.5F},
                                ::glm::vec3 {0.5F, 1.5F, -0.5F}));

                        camera = ::std::make_unique<::Components::Perspective>(
                                ::glm::vec3 {-6.0F, 3.798561F, -6.0F},
                                ::glm::vec3 {0.5110F, 0.8459F, -0.7924F},
                                ::glm::vec3 {0.0F, 1.0F, 0.0F},
                                fovX, fovY);
                    }

                    //Elvira_Holiday
                    if (::std::strstr(objFileName, "Elvira_Holiday") != nullptr) {
                        ::std::unique_ptr<MobileRT::Sampler> samplerPoint1{
                                ::std::make_unique<Components::StaticHaltonSeq>()};
                        scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                                lightMat,
                                ::std::move(samplerPoint1),
                                ::glm::vec3 {-50.5F, 41.5F, 50.5F},
                                ::glm::vec3 {50.5F, 41.5F, -50.5F},
                                ::glm::vec3 {50.5F, 41.5F, 50.5F}));
                        ::std::unique_ptr<MobileRT::Sampler> samplerPoint2{
                                ::std::make_unique<Components::StaticHaltonSeq>()};
                        scene_.lights_.emplace_back(::std::make_unique<::Components::AreaLight>(
                                lightMat,
                                ::std::move(samplerPoint2),
                                ::glm::vec3 {-50.5F, 41.5F, 50.5F},
                                ::glm::vec3 {-50.5F, 41.5F, -50.5F},
                                ::glm::vec3 {50.5F, 41.5F, -50.5F}));

                        camera = ::std::make_unique<::Components::Perspective>(
                                ::glm::vec3 {11.0754F - 50.0F, 22.8368F + 10.0F, -0.2654F - 50.0F},
                                ::glm::vec3 {11.0754F, 22.8368F, -0.2654F},
                                ::glm::vec3 {0.0F, 1.0F, 0.0F},
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
                samplerPixel = ::std::make_unique<Components::Constant>(0.5F);
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
            /*LOG("TRIANGLES = ", triangles);
            LOG("SPHERES = ", spheres);
            LOG("PLANES = ", planes);
            LOG("LIGHTS = ", numberOfLights_);*/
            return nPrimitives;
        }
    }()};


    env->ExceptionClear();
    LOG("PRIMITIVES = ", res);

    {
      const jboolean result {
              env->CallBooleanMethod(thiz, isLowMemoryMethodId, 1)};
      if (result) {
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
        const ::std::lock_guard<::std::mutex> lock{mutex_};
        state_ = State::FINISHED;
        LOG("WORKING = FINISHED");
        if (renderer_ != nullptr) {
            renderer_->stopRender();
        }
        if (thread_ != nullptr) {
            renderer_ = nullptr;
            thread_ = nullptr;
            LOG("DELETED RENDERER");
        }
        state_ = State::IDLE;
        LOG("WORKING = IDLE");
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
    jobject globalBitmap{static_cast<jobject>(env->NewGlobalRef(localBitmap))};

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
            FPS();
            rep--;
        }
        notified_ = true;
        rendered_.notify_all();
        {
            const ::std::lock_guard<::std::mutex> lock{mutex_};
            if (state_ != State::STOPPED) {
                state_ = State::FINISHED;
                LOG("WORKING = FINISHED");
            }
            {
                const ::std::int32_t result{AndroidBitmap_unlockPixels(env, globalBitmap)};
                assert(result == JNI_OK);
                static_cast<void> (result);
            }

            env->DeleteGlobalRef(globalBitmap);
            {
                const ::std::int32_t result{javaVM_->GetEnv(reinterpret_cast<void **>(const_cast<JNIEnv **>(&env)),
                                                            JNI_VERSION_1_6)};
                assert(result == JNI_OK);
                static_cast<void> (result);
            }
            env->ExceptionClear();
            if (jniError == JNI_EDETACHED) {
                const ::std::int32_t result{javaVM_->DetachCurrentThread()};
                assert(result == JNI_OK);
                static_cast<void> (result);
            }
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
::std::int32_t Java_puscas_mobilertapp_RenderTask_RTGetState(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    const ::std::int32_t res{static_cast<::std::int32_t> (state_.load())};
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
::std::int32_t Java_puscas_mobilertapp_MainActivity_RTResize(
        JNIEnv *env,
        jobject /*thiz*/,
        jint size
) noexcept {
    const ::std::int32_t res{::MobileRT::roundDownToMultipleOf(
            size, static_cast<::std::int32_t>(::std::sqrt(::MobileRT::NumberOfBlocks)))};
    env->ExceptionClear();
    return res;
}

extern "C"
::std::int32_t Java_puscas_mobilertapp_DrawView_RTGetNumberOfLights(
        JNIEnv *env,
        jobject /*thiz*/
) noexcept {
    env->ExceptionClear();
    return numberOfLights_;
}

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTFreeNativeBuffer(
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
