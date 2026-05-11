#include <jni.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include "vulkan_core.h"

#define LOG_TAG "VulkanBridge"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/**
 * 🟢 JNI function to initialize Vulkan.
 * Called from VulkanBridge.nativeInitVulkan()
 */
extern "C" JNIEXPORT jboolean JNICALL
Java_com_yourmod_VulkanBridge_nativeInitVulkan(JNIEnv *env, jobject thiz, jobject surfaceObj) {
    LOGD("nativeInitVulkan called");

    if (surfaceObj == nullptr) {
        LOGE("Surface object is null!");
        return JNI_FALSE;
    }

    // 🟡 Get the native window from the Java Surface object
    ANativeWindow* window = ANativeWindow_fromSurface(env, surfaceObj);
    if (window == nullptr) {
        LOGE("Failed to get ANativeWindow from Surface");
        return JNI_FALSE;
    }

    // 🟡 Initialize our Vulkan renderer with the native window
    bool result = initVulkan(window);
    if (!result) {
        LOGE("Failed to initialize Vulkan renderer");
        ANativeWindow_release(window);
        return JNI_FALSE;
    }

    LOGD("Vulkan renderer initialized successfully!");
    return JNI_TRUE;
}

/**
 * 🟢 JNI function to render a frame.
 * Called from VulkanBridge.nativeRenderFrame()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_yourmod_VulkanBridge_nativeRenderFrame(JNIEnv *env, jobject thiz) {
    renderFrame();
}

/**
 * 🟢 JNI function to clean up.
 * Called from VulkanBridge.nativeCleanup()
 */
extern "C" JNIEXPORT void JNICALL
Java_com_yourmod_VulkanBridge_nativeCleanup(JNIEnv *env, jobject thiz) {
    cleanupVulkan();
}
