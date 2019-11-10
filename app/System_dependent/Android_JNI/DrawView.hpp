#ifndef APP_DRAWVIEW_HPP
#define APP_DRAWVIEW_HPP

#include "Scenes/Scenes.hpp"
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
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <memory>
#include <thread>

enum class State {
    IDLE = 0, BUSY = 1, FINISHED = 2, STOPPED = 3
};


// JNI
extern "C"
jint JNI_OnLoad(JavaVM *pjvm, void *reserved);

extern "C"
void JNI_OnUnload(JavaVM *vm, void *reserved);


// DrawView
extern "C"
void Java_puscas_mobilertapp_DrawView_RTStopRender(
        JNIEnv *env,
        jobject thiz
) noexcept;

extern "C"
void Java_puscas_mobilertapp_DrawView_RTStartRender(
        JNIEnv *env,
        jobject thiz
) noexcept;

extern "C"
jint Java_puscas_mobilertapp_DrawView_RTGetNumberOfLights(
        JNIEnv *env,
        jobject thiz
) noexcept;


// ViewText
extern "C"
jint Java_puscas_mobilertapp_RenderTask_RTGetState(
        JNIEnv *env,
        jobject thiz
) noexcept;

extern "C"
jfloat Java_puscas_mobilertapp_RenderTask_RTGetFps(
        JNIEnv *env,
        jobject
) noexcept;

extern "C"
jlong Java_puscas_mobilertapp_RenderTask_RTGetTimeRenderer(
        JNIEnv *env,
        jobject thiz
) noexcept;

extern "C"
jint Java_puscas_mobilertapp_RenderTask_RTGetSample(
        JNIEnv *env,
        jobject thiz
) noexcept;


// MainActivity
extern "C"
jint Java_puscas_mobilertapp_MainActivity_RTResize(
        JNIEnv *env,
        jobject thiz,
        jint size
) noexcept;


// MainRenderer
extern "C"
void Java_puscas_mobilertapp_MainRenderer_RTFinishRender(
        JNIEnv *env,
        jobject thiz
) noexcept;

extern "C"
void Java_puscas_mobilertapp_MainRenderer_RTRenderIntoBitmap(
        JNIEnv *env,
        jobject thiz,
        jobject dstBitmap,
        jint nThreads,
        jboolean async
) noexcept;

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTInitVerticesArray(
        JNIEnv *env,
        jobject thiz
) noexcept;

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTInitColorsArray(
        JNIEnv *env,
        jobject thiz
) noexcept;

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTInitCameraArray(
        JNIEnv *env,
        jobject thiz
) noexcept;

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTFreeNativeBuffer(
        JNIEnv *env,
        jobject thiz,
        jobject bufferRef
) noexcept;

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
) noexcept;

#endif //APP_DRAWVIEW_HPP
