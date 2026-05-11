#pragma once

// This must be the first thing before any Vulkan includes
#define VK_USE_PLATFORM_ANDROID_KHR 1

#include <vulkan/vulkan.h>
#include <android/native_window.h>
#include <cstdint>
#include <vector>

struct VulkanData {
    VkInstance instance;
    VkPhysicalDevice physicalDevice;
    VkDevice device;
    VkQueue graphicsQueue;
    VkSurfaceKHR surface;
    VkSwapchainKHR swapchain;
    ANativeWindow* nativeWindow;
};

bool initVulkan(ANativeWindow* window);
void renderFrame();
void cleanupVulkan();
