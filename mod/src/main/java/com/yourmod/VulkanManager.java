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
        // In a real mod, surface will come from Minecraft's window
        // For now, just log
        VulkanMod.LOGGER.info("VulkanManager initialized");
        initialized = true;
    }

    public void renderFrame() {
        if (!initialized) return;
        // Call native render (if surface available)
        // VulkanBridge.nativeRenderFrame();
    }

    public void cleanup() {
        if (initialized) {
            VulkanBridge.nativeCleanup();
            initialized = false;
        }
    }
}
