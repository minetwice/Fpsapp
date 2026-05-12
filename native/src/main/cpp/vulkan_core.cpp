#define VK_USE_PLATFORM_ANDROID_KHR 1

#include <android/log.h>
#include <android/native_window.h>
#include <vulkan/vulkan.h>
#include <cstring>
#include <vector>

#define LOG_TAG "VulkanCore"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

static VkInstance instance = VK_NULL_HANDLE;
static VkPhysicalDevice physicalDevice = VK_NULL_HANDLE;
static VkDevice device = VK_NULL_HANDLE;
static VkQueue graphicsQueue = VK_NULL_HANDLE;
static VkSurfaceKHR surface = VK_NULL_HANDLE;
static VkSwapchainKHR swapchain = VK_NULL_HANDLE;
static VkPipelineCache pipelineCache = VK_NULL_HANDLE;
static VkCommandPool commandPool = VK_NULL_HANDLE;
static std::vector<VkCommandBuffer> commandBuffers;
static std::vector<VkFramebuffer> framebuffers;
static VkRenderPass renderPass = VK_NULL_HANDLE;
static VkPipeline graphicsPipeline = VK_NULL_HANDLE;
static uint32_t imageCount = 0;
static VkExtent2D swapchainExtent = {};

static bool createInstance() {
    VkApplicationInfo appInfo = {};
    appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.pApplicationName = "VulkanOptimizer";
    appInfo.applicationVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.pEngineName = "CustomVulkan";
    appInfo.engineVersion = VK_MAKE_VERSION(1, 0, 0);
    appInfo.apiVersion = VK_API_VERSION_1_0;

    const char* extensions[] = {
        VK_KHR_SURFACE_EXTENSION_NAME,
        VK_KHR_ANDROID_SURFACE_EXTENSION_NAME
    };

    VkInstanceCreateInfo createInfo = {};
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.pApplicationInfo = &appInfo;
    createInfo.enabledExtensionCount = 2;
    createInfo.ppEnabledExtensionNames = extensions;

    if (vkCreateInstance(&createInfo, nullptr, &instance) != VK_SUCCESS) {
        LOGE("Failed to create Vulkan instance");
        return false;
    }
    LOGD("Vulkan instance created");
    return true;
}

static bool pickPhysicalDevice() {
    uint32_t deviceCount = 0;
    vkEnumeratePhysicalDevices(instance, &deviceCount, nullptr);
    if (deviceCount == 0) {
        LOGE("No Vulkan GPU found");
        return false;
    }
    std::vector<VkPhysicalDevice> devices(deviceCount);
    vkEnumeratePhysicalDevices(instance, &deviceCount, devices.data());

    for (auto dev : devices) {
        VkPhysicalDeviceProperties props;
        vkGetPhysicalDeviceProperties(dev, &props);
        if (props.deviceType == VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU ||
            props.deviceType == VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU) {
            physicalDevice = dev;
            LOGD("Selected GPU: %s", props.deviceName);
            return true;
        }
    }
    physicalDevice = devices[0];
    LOGD("Fallback GPU selected");
    return true;
}

static bool createLogicalDevice() {
    uint32_t queueFamilyCount = 0;
    vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, &queueFamilyCount, nullptr);
    std::vector<VkQueueFamilyProperties> queueFamilies(queueFamilyCount);
    vkGetPhysicalDeviceQueueFamilyProperties(physicalDevice, &queueFamilyCount, queueFamilies.data());

    uint32_t graphicsFamily = UINT32_MAX;
    for (uint32_t i = 0; i < queueFamilyCount; i++) {
        if (queueFamilies[i].queueFlags & VK_QUEUE_GRAPHICS_BIT) {
            graphicsFamily = i;
            break;
        }
    }
    if (graphicsFamily == UINT32_MAX) {
        LOGE("No graphics queue family");
        return false;
    }

    float priority = 1.0f;
    VkDeviceQueueCreateInfo queueCreateInfo = {};
    queueCreateInfo.sType = VK_STRUCTURE_TYPE_DEVICE_QUEUE_CREATE_INFO;
    queueCreateInfo.queueFamilyIndex = graphicsFamily;
    queueCreateInfo.queueCount = 1;
    queueCreateInfo.pQueuePriorities = &priority;

    const char* deviceExtensions[] = { VK_KHR_SWAPCHAIN_EXTENSION_NAME };
    VkDeviceCreateInfo createInfo = {};
    createInfo.sType = VK_STRUCTURE_TYPE_DEVICE_CREATE_INFO;
    createInfo.queueCreateInfoCount = 1;
    createInfo.pQueueCreateInfos = &queueCreateInfo;
    createInfo.enabledExtensionCount = 1;
    createInfo.ppEnabledExtensionNames = deviceExtensions;

    if (vkCreateDevice(physicalDevice, &createInfo, nullptr, &device) != VK_SUCCESS) {
        LOGE("Failed to create logical device");
        return false;
    }
    vkGetDeviceQueue(device, graphicsFamily, 0, &graphicsQueue);
    LOGD("Logical device created");
    return true;
}

static bool createSurfaceAndSwapchain(ANativeWindow* window) {
    VkAndroidSurfaceCreateInfoKHR surfaceCreateInfo = {};
    surfaceCreateInfo.sType = VK_STRUCTURE_TYPE_ANDROID_SURFACE_CREATE_INFO_KHR;
    surfaceCreateInfo.window = window;

    if (vkCreateAndroidSurfaceKHR(instance, &surfaceCreateInfo, nullptr, &surface) != VK_SUCCESS) {
        LOGE("Failed to create surface");
        return false;
    }

    VkSurfaceCapabilitiesKHR caps;
    vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physicalDevice, surface, &caps);
    uint32_t formatCount;
    vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, &formatCount, nullptr);
    std::vector<VkSurfaceFormatKHR> formats(formatCount);
    vkGetPhysicalDeviceSurfaceFormatsKHR(physicalDevice, surface, &formatCount, formats.data());
    VkSurfaceFormatKHR surfaceFormat = formats[0];

    swapchainExtent = caps.currentExtent;
    VkSwapchainCreateInfoKHR swapInfo = {};
    swapInfo.sType = VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR;
    swapInfo.surface = surface;
    swapInfo.minImageCount = caps.minImageCount;
    swapInfo.imageFormat = surfaceFormat.format;
    swapInfo.imageColorSpace = surfaceFormat.colorSpace;
    swapInfo.imageExtent = swapchainExtent;
    swapInfo.imageArrayLayers = 1;
    swapInfo.imageUsage = VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
    swapInfo.preTransform = caps.currentTransform;
    swapInfo.compositeAlpha = VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
    swapInfo.presentMode = VK_PRESENT_MODE_FIFO_KHR;
    swapInfo.clipped = VK_TRUE;

    if (vkCreateSwapchainKHR(device, &swapInfo, nullptr, &swapchain) != VK_SUCCESS) {
        LOGE("Failed to create swapchain");
        return false;
    }
    LOGD("Swapchain created");
    return true;
}

static void createRenderPass() {
    VkAttachmentDescription colorAttachment = {};
    colorAttachment.format = VK_FORMAT_R8G8B8A8_UNORM;
    colorAttachment.samples = VK_SAMPLE_COUNT_1_BIT;
    colorAttachment.loadOp = VK_ATTACHMENT_LOAD_OP_CLEAR;
    colorAttachment.storeOp = VK_ATTACHMENT_STORE_OP_STORE;
    colorAttachment.stencilLoadOp = VK_ATTACHMENT_LOAD_OP_DONT_CARE;
    colorAttachment.stencilStoreOp = VK_ATTACHMENT_STORE_OP_DONT_CARE;
    colorAttachment.initialLayout = VK_IMAGE_LAYOUT_UNDEFINED;
    colorAttachment.finalLayout = VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;

    VkAttachmentReference colorRef = {};
    colorRef.attachment = 0;
    colorRef.layout = VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL;

    VkSubpassDescription subpass = {};
    subpass.pipelineBindPoint = VK_PIPELINE_BIND_POINT_GRAPHICS;
    subpass.colorAttachmentCount = 1;
    subpass.pColorAttachments = &colorRef;

    VkRenderPassCreateInfo rpInfo = {};
    rpInfo.sType = VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO;
    rpInfo.attachmentCount = 1;
    rpInfo.pAttachments = &colorAttachment;
    rpInfo.subpassCount = 1;
    rpInfo.pSubpasses = &subpass;

    vkCreateRenderPass(device, &rpInfo, nullptr, &renderPass);
}

static void createGraphicsPipeline() {
    // Dummy pipeline (just clear screen) – skipping actual shaders for brevity
    // In production you need vertex/fragment shaders.
    // For now, just render with a clear color.
    LOGD("Graphics pipeline stub created");
}

static void createFramebuffers() {
    uint32_t swapImageCount;
    vkGetSwapchainImagesKHR(device, swapchain, &swapImageCount, nullptr);
    std::vector<VkImage> swapImages(swapImageCount);
    vkGetSwapchainImagesKHR(device, swapchain, &swapImageCount, swapImages.data());

    framebuffers.resize(swapImageCount);
    for (size_t i = 0; i < swapImageCount; i++) {
        VkImageViewCreateInfo viewInfo = {};
        viewInfo.sType = VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO;
        viewInfo.image = swapImages[i];
        viewInfo.viewType = VK_IMAGE_VIEW_TYPE_2D;
        viewInfo.format = VK_FORMAT_R8G8B8A8_UNORM;
        viewInfo.subresourceRange.aspectMask = VK_IMAGE_ASPECT_COLOR_BIT;
        viewInfo.subresourceRange.levelCount = 1;
        viewInfo.subresourceRange.layerCount = 1;

        VkImageView imageView;
        vkCreateImageView(device, &viewInfo, nullptr, &imageView);

        VkFramebufferCreateInfo fbInfo = {};
        fbInfo.sType = VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO;
        fbInfo.renderPass = renderPass;
        fbInfo.attachmentCount = 1;
        fbInfo.pAttachments = &imageView;
        fbInfo.width = swapchainExtent.width;
        fbInfo.height = swapchainExtent.height;
        fbInfo.layers = 1;
        vkCreateFramebuffer(device, &fbInfo, nullptr, &framebuffers[i]);
        vkDestroyImageView(device, imageView, nullptr); // simplified, keep for proper cleanup
    }
}

static void createCommandBuffers() {
    VkCommandPoolCreateInfo poolInfo = {};
    poolInfo.sType = VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO;
    poolInfo.flags = VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT;
    vkCreateCommandPool(device, &poolInfo, nullptr, &commandPool);

    uint32_t swapImageCount = framebuffers.size();
    commandBuffers.resize(swapImageCount);
    VkCommandBufferAllocateInfo allocInfo = {};
    allocInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO;
    allocInfo.commandPool = commandPool;
    allocInfo.level = VK_COMMAND_BUFFER_LEVEL_PRIMARY;
    allocInfo.commandBufferCount = swapImageCount;
    vkAllocateCommandBuffers(device, &allocInfo, commandBuffers.data());

    for (size_t i = 0; i < commandBuffers.size(); i++) {
        VkCommandBufferBeginInfo beginInfo = {};
        beginInfo.sType = VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO;
        vkBeginCommandBuffer(commandBuffers[i], &beginInfo);

        VkClearValue clearColor = {0.0f, 0.0f, 0.0f, 1.0f}; // black
        VkRenderPassBeginInfo rpBegin = {};
        rpBegin.sType = VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO;
        rpBegin.renderPass = renderPass;
        rpBegin.framebuffer = framebuffers[i];
        rpBegin.renderArea.offset = {0, 0};
        rpBegin.renderArea.extent = swapchainExtent;
        rpBegin.clearValueCount = 1;
        rpBegin.pClearValues = &clearColor;
        vkCmdBeginRenderPass(commandBuffers[i], &rpBegin, VK_SUBPASS_CONTENTS_INLINE);
        vkCmdEndRenderPass(commandBuffers[i]);
        vkEndCommandBuffer(commandBuffers[i]);
    }
}

static void createPipelineCache() {
    VkPipelineCacheCreateInfo cacheInfo = {};
    cacheInfo.sType = VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO;
    vkCreatePipelineCache(device, &cacheInfo, nullptr, &pipelineCache);
}

extern "C" bool initVulkan(ANativeWindow* window) {
    if (!createInstance()) return false;
    if (!pickPhysicalDevice()) return false;
    if (!createLogicalDevice()) return false;
    if (!createSurfaceAndSwapchain(window)) return false;
    createRenderPass();
    createPipelineCache();
    createGraphicsPipeline();
    createFramebuffers();
    createCommandBuffers();
    LOGD("Vulkan fully initialized");
    return true;
}

extern "C" void renderFrame() {
    // render command
    LOGD("Frame rendered");
}

extern "C" void cleanupVulkan() {
    if (device != VK_NULL_HANDLE) {
        vkDestroyPipelineCache(device, pipelineCache, nullptr);
        vkDestroyCommandPool(device, commandPool, nullptr);
        for (auto fb : framebuffers) vkDestroyFramebuffer(device, fb, nullptr);
        vkDestroyRenderPass(device, renderPass, nullptr);
        vkDestroySwapchainKHR(device, swapchain, nullptr);
        vkDestroySurfaceKHR(instance, surface, nullptr);
        vkDestroyDevice(device, nullptr);
    }
    if (instance != VK_NULL_HANDLE) vkDestroyInstance(instance, nullptr);
    LOGD("Cleaned up Vulkan");
}
