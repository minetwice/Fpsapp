#include <jni.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "vulkan_core.h"

#define LOG_TAG "VulkanBridge"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jboolean JNICALL
Java_com_yourmod_VulkanBridge_nativeInitVulkan(JNIEnv *env, jobject thiz, jobject surfaceObj) {
    LOGD("nativeInitVulkan called");
    ANativeWindow* window = ANativeWindow_fromSurface(env, surfaceObj);
    if (!window) return JNI_FALSE;
    bool ok = initVulkan(window);
    if (!ok) ANativeWindow_release(window);
    return ok ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_yourmod_VulkanBridge_nativeRenderFrame(JNIEnv *env, jobject thiz) {
    renderFrame();
}

JNIEXPORT void JNICALL
Java_com_yourmod_VulkanBridge_nativeCleanup(JNIEnv *env, jobject thiz) {
    cleanupVulkan();
}

JNIEXPORT void JNICALL
Java_com_yourmod_VulkanBridge_setOptimizationFlags(JNIEnv *env, jobject thiz,
    jboolean entities, jboolean blocks, jboolean hits, jboolean camera, jboolean replay, jboolean highPerf) {
    setOptimizationFlags(entities, blocks, hits, camera, replay, highPerf);
}

JNIEXPORT void JNICALL
Java_com_yourmod_VulkanBridge_setTargetFPS(JNIEnv *env, jobject thiz, jint fps) {
    setTargetFPS(fps);
}

JNIEXPORT void JNICALL
Java_com_yourmod_VulkanBridge_applyRealtimeOptimizations(JNIEnv *env, jobject thiz) {
    applyRealtimeOptimizations();
}

JNIEXPORT void JNICALL
Java_com_yourmod_VulkanBridge_onBlockPlaceEvent(JNIEnv *env, jobject thiz) {
    onBlockPlaceEvent();
}

JNIEXPORT void JNICALL
Java_com_yourmod_VulkanBridge_onHitEvent(JNIEnv *env, jobject thiz) {
    onHitEvent();
}

JNIEXPORT void JNICALL
Java_com_yourmod_VulkanBridge_onCameraMove(JNIEnv *env, jobject thiz, jfloat deltaX, jfloat deltaY) {
    onCameraMove(deltaX, deltaY);
}

#ifdef __cplusplus
}
#endif
