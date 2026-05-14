#include <jni.h>
#include <android/log.h>

#define LOG_TAG "VulkanBridge"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jboolean JNICALL
Java_com_yourlauncher_VulkanBridge_nativeInitVulkan(JNIEnv *env, jobject thiz, jobject surfaceObj) {
    LOGD("nativeInitVulkan called (dummy)");
    return JNI_TRUE;
}

JNIEXPORT void JNICALL
Java_com_yourlauncher_VulkanBridge_nativeRenderFrame(JNIEnv *env, jobject thiz) {
    LOGD("nativeRenderFrame called (dummy)");
}

JNIEXPORT void JNICALL
Java_com_yourlauncher_VulkanBridge_nativeCleanup(JNIEnv *env, jobject thiz) {
    LOGD("nativeCleanup called (dummy)");
}

JNIEXPORT void JNICALL
Java_com_yourlauncher_VulkanBridge_setOptimizationFlags(JNIEnv *env, jobject thiz,
    jboolean entities, jboolean blocks, jboolean hits, jboolean camera, jboolean replay, jboolean highPerf) {
    LOGD("setOptimizationFlags (dummy)");
}

JNIEXPORT void JNICALL
Java_com_yourlauncher_VulkanBridge_setTargetFPS(JNIEnv *env, jobject thiz, jint fps) {
    LOGD("setTargetFPS (dummy): %d", fps);
}

JNIEXPORT void JNICALL
Java_com_yourlauncher_VulkanBridge_applyRealtimeOptimizations(JNIEnv *env, jobject thiz) {
    LOGD("applyRealtimeOptimizations (dummy)");
}

JNIEXPORT void JNICALL
Java_com_yourlauncher_VulkanBridge_enableMultiThreading(JNIEnv *env, jobject thiz, jboolean enable) {
    LOGD("enableMultiThreading (dummy): %d", enable);
}

JNIEXPORT void JNICALL
Java_com_yourlauncher_VulkanBridge_enableDynamicResolution(JNIEnv *env, jobject thiz, jboolean enable) {
    LOGD("enableDynamicResolution (dummy): %d", enable);
}

JNIEXPORT void JNICALL
Java_com_yourlauncher_VulkanBridge_onBlockPlaceEvent(JNIEnv *env, jobject thiz) {
    LOGD("onBlockPlaceEvent (dummy)");
}

JNIEXPORT void JNICALL
Java_com_yourlauncher_VulkanBridge_onHitEvent(JNIEnv *env, jobject thiz) {
    LOGD("onHitEvent (dummy)");
}

JNIEXPORT void JNICALL
Java_com_yourlauncher_VulkanBridge_onCameraMove(JNIEnv *env, jobject thiz, jfloat deltaX, jfloat deltaY) {
    LOGD("onCameraMove (dummy): %.2f, %.2f", deltaX, deltaY);
}

} // extern "C"
