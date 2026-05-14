#include <android/log.h>
#include <android/native_window.h>

#define LOG_TAG "VulkanCore"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)

extern "C" {

bool initVulkan(ANativeWindow* window) {
    LOGD("initVulkan stub");
    return true;
}

void renderFrame() {
    LOGD("renderFrame stub");
}

void cleanupVulkan() {
    LOGD("cleanupVulkan stub");
}

void setOptimizationFlags(bool entities, bool blocks, bool hits, bool camera, bool replay, bool highPerf) {
    LOGD("setOptimizationFlags stub");
}

void setTargetFPS(int fps) {
    LOGD("setTargetFPS stub: %d", fps);
}

void applyRealtimeOptimizations() {
    LOGD("applyRealtimeOptimizations stub");
}

void enableMultiThreading(bool enable) {
    LOGD("enableMultiThreading stub: %d", enable);
}

void enableDynamicResolution(bool enable) {
    LOGD("enableDynamicResolution stub: %d", enable);
}

void onBlockPlaceEvent() {
    LOGD("onBlockPlaceEvent stub");
}

void onHitEvent() {
    LOGD("onHitEvent stub");
}

void onCameraMove(float deltaX, float deltaY) {
    LOGD("onCameraMove stub: %.2f, %.2f", deltaX, deltaY);
}

} // extern "C"
