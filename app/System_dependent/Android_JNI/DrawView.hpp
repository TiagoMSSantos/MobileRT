#ifndef APP_DRAWVIEW_HPP
#define APP_DRAWVIEW_HPP

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
);

extern "C"
void Java_puscas_mobilertapp_DrawView_RTStartRender(
        JNIEnv *env,
        jobject thiz
);

extern "C"
jint Java_puscas_mobilertapp_DrawView_RTGetNumberOfLights(
        JNIEnv *env,
        jobject thiz
);


// ViewText
extern "C"
jint Java_puscas_mobilertapp_RenderTask_RTGetState(
        JNIEnv *env,
        jobject thiz
);

extern "C"
jfloat Java_puscas_mobilertapp_RenderTask_RTGetFps(
        JNIEnv *env,
        jobject
);

extern "C"
jlong Java_puscas_mobilertapp_RenderTask_RTGetTimeRenderer(
        JNIEnv *env,
        jobject thiz
);

extern "C"
jint Java_puscas_mobilertapp_RenderTask_RTGetSample(
        JNIEnv *env,
        jobject thiz
);


// MainActivity
extern "C"
jint Java_puscas_mobilertapp_MainActivity_RTResize(
        JNIEnv *env,
        jobject thiz,
        jint size
);


// MainRenderer
extern "C"
void Java_puscas_mobilertapp_MainRenderer_RTFinishRender(
        JNIEnv *env,
        jobject thiz
);

extern "C"
void Java_puscas_mobilertapp_MainRenderer_RTRenderIntoBitmap(
        JNIEnv *env,
        jobject thiz,
        jobject dstBitmap,
        jint nThreads,
        jboolean async
);

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTInitVerticesArray(
        JNIEnv *env,
        jobject thiz
);

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTInitColorsArray(
        JNIEnv *env,
        jobject thiz
);

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTInitCameraArray(
        JNIEnv *env,
        jobject thiz
);

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_RTFreeNativeBuffer(
        JNIEnv *env,
        jobject thiz,
        jobject bufferRef
);

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
);

#endif //APP_DRAWVIEW_HPP
