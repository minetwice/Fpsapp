#pragma once

#define VK_USE_PLATFORM_ANDROID_KHR 1

#include <vulkan/vulkan.h>
#include <android/native_window.h>
#include <cstdint>
#include <vector>

#ifdef __cplusplus
extern "C" {
#endif

// Core Vulkan lifecycle
bool initVulkan(ANativeWindow* window);
void renderFrame();
void cleanupVulkan();

// Performance optimization flags
void setOptimizationFlags(bool entities, bool blocks, bool hits, bool camera, bool replay, bool highPerf);
void setTargetFPS(int fps);
void applyRealtimeOptimizations();
void onBlockPlaceEvent();
void onHitEvent();
void onCameraMove(float deltaX, float deltaY);

#ifdef __cplusplus
}
#endif
