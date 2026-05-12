#pragma once

#define VK_USE_PLATFORM_ANDROID_KHR 1

#include <vulkan/vulkan.h>
#include <android/native_window.h>
#include <cstdint>
#include <vector>

#ifdef __cplusplus
extern "C" {
#endif

bool initVulkan(ANativeWindow* window);
void renderFrame();
void cleanupVulkan();
void setIndirectDrawEnabled(bool enabled);
void setMultiThreadedRendering(bool enabled);
void setTargetFPS(int fps);

// Add these feature flags at the top, after the includes
extern bool g_optimizeEntityRendering;
extern bool g_optimizeBlockPlacement;
extern bool g_optimizeHitDetection;
extern bool g_optimizeCameraMovement;
extern bool g_fixReplayLag;
extern bool g_highPerformanceMode;

// Add these function declarations
extern "C" void setOptimizationFlags(bool entities, bool blocks, bool hits, bool camera, bool replay, bool highPerf);
extern "C" void applyRealtimeOptimizations();
extern "C" void onBlockPlaceEvent();
extern "C" void onHitEvent();
extern "C" void onCameraMove(float deltaX, float deltaY);

#ifdef __cplusplus
}
#endif
