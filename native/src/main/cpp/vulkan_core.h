#pragma once

#include <vulkan/vulkan.h>
#include <android/native_window.h>
#include <cstdint>
#include <vector>

// Ensure Android platform extensions are enabled
#define VK_USE_PLATFORM_ANDROID_KHR
#include <vulkan/vulkan.h>  // Include again after define (or move define before first include)

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
