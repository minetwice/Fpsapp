package com.yourmod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VulkanBridge {
    private static final Logger LOGGER = LoggerFactory.getLogger("VulkanBridge");
    
    static {
        try {
            System.loadLibrary("vulkan_renderer");
            LOGGER.info("Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            LOGGER.error("Failed to load vulkan_renderer from library path: " + e.getMessage());
            // Try to load from absolute path (if you know where the launcher extracts libs)
            try {
                System.load("/data/data/git.artdeell.mojo/cache/natives/libvulkan_renderer.so");
                LOGGER.info("Loaded from absolute path");
            } catch (Throwable t) {
                LOGGER.error("Also failed from absolute path: " + t.getMessage());
            }
        }
    }

    // Core methods
    public static native boolean nativeInitVulkan(Object surface);
    public static native void nativeRenderFrame();
    public static native void nativeCleanup();

    // Optimization methods
    public static native void setOptimizationFlags(boolean entities, boolean blocks, boolean hits, boolean camera, boolean replay, boolean highPerf);
    public static native void setTargetFPS(int fps);
    public static native void applyRealtimeOptimizations();
    public static native void onBlockPlaceEvent();
    public static native void onHitEvent();
    public static native void onCameraMove(float deltaX, float deltaY);
}
