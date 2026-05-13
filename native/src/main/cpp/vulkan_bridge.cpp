package com.yourmod;

public class VulkanBridge {
    static {
        System.loadLibrary("vulkan_renderer");
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
