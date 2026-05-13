package com.yourmod;

public class VulkanManager {
    private static VulkanManager instance;
    private boolean initialized = false;

    public static synchronized VulkanManager getInstance() {
        if (instance == null) instance = new VulkanManager();
        return instance;
    }

    public void init() {
        if (initialized) return;
        VulkanMod.LOGGER.info("VulkanManager initialized");
        initialized = true;
    }

    public void renderFrame() {
        if (!initialized) return;
        VulkanBridge.nativeRenderFrame();
    }

    public void cleanup() {
        if (initialized) {
            VulkanBridge.nativeCleanup();
            initialized = false;
        }
    }
}
