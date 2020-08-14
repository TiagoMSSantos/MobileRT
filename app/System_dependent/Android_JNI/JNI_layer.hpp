#ifndef APP_JNI_LAYER_HPP
#define APP_JNI_LAYER_HPP

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <android/bitmap.h>
#include <android/log.h>
#include <jni.h>
#include <memory>
#include <thread>

enum class State {
    IDLE = 0, BUSY = 1, FINISHED = 2, STOPPED = 3
};


// JNI
extern "C"
jint JNI_OnLoad(JavaVM *jvm, void *reserved);

extern "C"
void JNI_OnUnload(JavaVM *vm, void *reserved);


// DrawView
extern "C"
void Java_puscas_mobilertapp_DrawView_rtStopRender(
        JNIEnv *env,
        jobject thiz,
        jboolean wait
);

extern "C"
void Java_puscas_mobilertapp_DrawView_rtStartRender(
        JNIEnv *env,
        jobject thiz,
        jboolean wait
);

extern "C"
jint Java_puscas_mobilertapp_DrawView_rtGetNumberOfLights(
        JNIEnv *env,
        jobject thiz
);


// ViewText
extern "C"
jint Java_puscas_mobilertapp_RenderTask_rtGetState(
        JNIEnv *env,
        jobject thiz
);

extern "C"
jfloat Java_puscas_mobilertapp_RenderTask_rtGetFps(
        JNIEnv *env,
        jobject
);

extern "C"
jlong Java_puscas_mobilertapp_RenderTask_rtGetTimeRenderer(
        JNIEnv *env,
        jobject thiz
);

extern "C"
jint Java_puscas_mobilertapp_RenderTask_rtGetSample(
        JNIEnv *env,
        jobject thiz
);


// MainActivity
extern "C"
JNIEXPORT
jint JNICALL Java_puscas_mobilertapp_MainActivity_rtResize(
        JNIEnv *env,
        jobject thiz,
        jint size
);


// MainRenderer
extern "C"
void Java_puscas_mobilertapp_MainRenderer_rtFinishRender(
        JNIEnv *env,
        jobject thiz
);

extern "C"
void Java_puscas_mobilertapp_MainRenderer_rtRenderIntoBitmap(
        JNIEnv *env,
        jobject thiz,
        jobject localBitmap,
        jint nThreads,
        jboolean async
);

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_rtInitVerticesArray(
        JNIEnv *env,
        jobject thiz
);

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_rtInitColorsArray(
        JNIEnv *env,
        jobject thiz
);

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_rtInitCameraArray(
        JNIEnv *env,
        jobject thiz
);

extern "C"
jobject Java_puscas_mobilertapp_MainRenderer_rtFreeNativeBuffer(
        JNIEnv *env,
        jobject thiz,
        jobject bufferRef
);

extern "C"
jint Java_puscas_mobilertapp_MainRenderer_rtInitialize(
        JNIEnv *env,
        jobject thiz,
        jobject localConfig
);

#endif //APP_JNI_LAYER_HPP
