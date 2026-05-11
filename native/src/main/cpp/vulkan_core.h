#pragma once

#include <vulkan/vulkan.h>
#include <android/native_window.h>

// 🟡 Basic Vulkan data structures for our Stage 1 renderer
struct VulkanData {
    VkInstance instance;
    VkPhysicalDevice physicalDevice;
    VkDevice device;
    VkQueue graphicsQueue;
    VkSurfaceKHR surface;
    VkSwapchainKHR swapchain;
    ANativeWindow* nativeWindow;
};

// 🟡 Function declarations
bool initVulkan(ANativeWindow* window);
void renderFrame();
void cleanupVulkan();
