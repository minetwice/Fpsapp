#define VK_USE_PLATFORM_ANDROID_KHR 1

#include <jni.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "vulkan_core.h"

#define LOG_TAG "VulkanBridge"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

extern "C" JNIEXPORT jboolean JNICALL
Java_com_yourmod_VulkanBridge_nativeInitVulkan(JNIEnv *env, jobject thiz, jobject surfaceObj) {
    LOGD("nativeInitVulkan called");

    if (surfaceObj == nullptr) {
        LOGE("Surface object is null!");
        return JNI_FALSE;
    }

    ANativeWindow* window = ANativeWindow_fromSurface(env, surfaceObj);
    if (window == nullptr) {
        LOGE("Failed to get ANativeWindow from Surface");
        return JNI_FALSE;
    }

    bool result = initVulkan(window);
    if (!result) {
        LOGE("Failed to initialize Vulkan renderer");
        ANativeWindow_release(window);
        return JNI_FALSE;
    }

    LOGD("Vulkan renderer initialized successfully!");
    return JNI_TRUE;
}

extern "C" JNIEXPORT void JNICALL
Java_com_yourmod_VulkanBridge_nativeRenderFrame(JNIEnv *env, jobject thiz) {
    renderFrame();
}

extern "C" JNIEXPORT void JNICALL
Java_com_yourmod_VulkanBridge_nativeCleanup(JNIEnv *env, jobject thiz) {
    cleanupVulkan();
}
