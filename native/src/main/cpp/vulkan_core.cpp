#include <android/log.h>
#include "vulkan_core.h"

#define LOG_TAG "VulkanCore"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static VulkanData vkData = {};
static bool initialized = false;

static bool createInstance() {
    VkApplicationInfo appInfo = {};
    appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.pApplicationName = "VulkanMinecraftMod";
    appInfo.applicationVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.pEngineName = "CustomVulkanEngine";
    appInfo.engineVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.apiVersion = VK_API_VERSION_1_0;

    VkInstanceCreateInfo createInfo = {};
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.pApplicationInfo = &appInfo;

    const char* extensions[] = {
        VK_KHR_SURFACE_EXTENSION_NAME,
        VK_KHR_ANDROID_SURFACE_EXTENSION_NAME
    };
    createInfo.enabledExtensionCount = 2;
    createInfo.ppEnabledExtensionNames = extensions;

    if (vkCreateInstance(&createInfo, nullptr, &vkData.instance) != VK_SUCCESS) {
        LOGE("Failed to create Vulkan instance");
        return false;
    }

    LOGD("Vulkan instance created");
    return true;
}

static bool pickPhysicalDevice() {
    uint32_t deviceCount = 0;
    vkEnumeratePhysicalDevices(vkData.instance, &deviceCount, nullptr);
    if (deviceCount == 0) {
        LOGE("No Vulkan-capable GPUs found");
        return false;
    }

    std::vector<VkPhysicalDevice> devices(deviceCount);
    vkEnumeratePhysicalDevices(vkData.instance, &deviceCount, devices.data());

    for (const auto& device : devices) {
        VkPhysicalDeviceProperties deviceProperties;
        vkGetPhysicalDeviceProperties(device, &deviceProperties);
        if (deviceProperties.deviceType == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU ||
            deviceProperties.deviceType == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU) {
            vkData.physicalDevice = device;
            LOGD("Selected GPU: %s", deviceProperties.deviceName);
            return true;
        }
    }

    vkData.physicalDevice = devices[0];
    LOGD("Selected fallback GPU");
    return true;
}

static bool createLogicalDevice() {
    float queuePriority = 1.0f;
    VkDeviceQueueCreateInfo queueCreateInfo = {};
    queueCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
    queueCreateInfo.queueFamilyIndex = 0;
    queueCreateInfo.queueCount = 1;
    queueCreateInfo.pQueuePriorities = &queuePriority;

    VkDeviceCreateInfo deviceCreateInfo = {};
    deviceCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
    deviceCreateInfo.pQueueCreateInfos = &queueCreateInfo;
    deviceCreateInfo.queueCreateInfoCount = 1;

    const char* deviceExtensions[] = {
        VK_KHR_SWAPCHAIN_EXTENSION_NAME
    };
    deviceCreateInfo.enabledExtensionCount = 1;
    deviceCreateInfo.ppEnabledExtensionNames = deviceExtensions;

    if (vkCreateDevice(vkData.physicalDevice, &deviceCreateInfo, nullptr, &vkData.device) != VK_SUCCESS) {
        LOGE("Failed to create logical device");
        return false;
    }

    vkGetDeviceQueue(vkData.device, 0, 0, &vkData.graphicsQueue);
    LOGD("Logical device created");
    return true;
}

static bool createSurfaceAndSwapchain(ANativeWindow* window) {
    VkAndroidSurfaceCreateInfoKHR surfaceCreateInfo = {};
    surfaceCreateInfo.sType = VK_STRUCTURE_TYPE_ANDROID_SURFACE_CREATE_INFO_KHR;
    surfaceCreateInfo.window = window;

    if (vkCreateAndroidSurfaceKHR(vkData.instance, &surfaceCreateInfo, nullptr, &vkData.surface) != VK_SUCCESS) {
        LOGE("Failed to create Android surface");
        return false;
    }

    VkSurfaceCapabilitiesKHR capabilities;
    vkGetPhysicalDeviceSurfaceCapabilitiesKHR(vkData.physicalDevice, vkData.surface, &capabilities);

    uint32_t formatCount;
    vkGetPhysicalDeviceSurfaceFormatsKHR(vkData.physicalDevice, vkData.surface, &formatCount, nullptr);
    std::vector<VkSurfaceFormatKHR> formats(formatCount);
    vkGetPhysicalDeviceSurfaceFormatsKHR(vkData.physicalDevice, vkData.surface, &formatCount, formats.data());
    VkSurfaceFormatKHR surfaceFormat = formats[0];

    VkSwapchainCreateInfoKHR swapchainCreateInfo = {};
    swapchainCreateInfo.sType = VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
    swapchainCreateInfo.surface = vkData.surface;
    swapchainCreateInfo.minImageCount = capabilities.minImageCount;
    swapchainCreateInfo.imageFormat = surfaceFormat.format;
    swapchainCreateInfo.imageColorSpace = surfaceFormat.colorSpace;
    swapchainCreateInfo.imageExtent = capabilities.currentExtent;
    swapchainCreateInfo.imageArrayLayers = 1;
    swapchainCreateInfo.imageUsage = VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;

    if (vkCreateSwapchainKHR(vkData.device, &swapchainCreateInfo, nullptr, &vkData.swapchain) != VK_SUCCESS) {
        LOGE("Failed to create swapchain");
        return false;
    }

    LOGD("Surface and swapchain created");
    return true;
}

bool initVulkan(ANativeWindow* window) {
    if (initialized) return true;

    LOGD("Initializing Vulkan renderer...");
    vkData.nativeWindow = window;

    if (!createInstance()) return false;
    if (!pickPhysicalDevice()) return false;
    if (!createLogicalDevice()) return false;
    if (!createSurfaceAndSwapchain(window)) return false;

    initialized = true;
    LOGD("Vulkan renderer ready");
    return true;
}

void renderFrame() {
    if (!initialized) {
        LOGE("Cannot render: Vulkan not initialized");
        return;
    }
    LOGD("Rendering frame...");
}

void cleanupVulkan() {
    if (!initialized) return;

    LOGD("Cleaning up Vulkan resources...");
    if (vkData.swapchain != VK_NULL_HANDLE) {
        vkDestroySwapchainKHR(vkData.device, vkData.swapchain, nullptr);
    }
    if (vkData.surface != VK_NULL_HANDLE) {
        vkDestroySurfaceKHR(vkData.instance, vkData.surface, nullptr);
    }
    if (vkData.device != VK_NULL_HANDLE) {
        vkDestroyDevice(vkData.device, nullptr);
    }
    if (vkData.instance != VK_NULL_HANDLE) {
        vkDestroyInstance(vkData.instance, nullptr);
    }
    initialized = false;
    LOGD("Cleanup complete");
}
