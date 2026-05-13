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
        VulkanMod.LOGGER.info("VulkanManager initializing");
        // No automatic native init here; surface will be provided by mixin
        initialized = true;
    }

    public void renderFrame() {
        if (!initialized) return;
        try {
            VulkanBridge.nativeRenderFrame();
        } catch (UnsatisfiedLinkError e) {
            VulkanMod.LOGGER.error("Render frame failed: " + e.getMessage());
        }
    }

    public void cleanup() {
        if (initialized) {
            try { VulkanBridge.nativeCleanup(); } catch (UnsatisfiedLinkError e) {}
            initialized = false;
        }
    }
}
