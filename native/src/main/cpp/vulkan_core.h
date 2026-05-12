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

#ifdef __cplusplus
}
#endif
