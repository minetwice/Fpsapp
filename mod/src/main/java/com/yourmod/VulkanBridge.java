package com.yourmod;

public class VulkanBridge {
    static {
        System.loadLibrary("vulkan_renderer");
    }

    public static native boolean nativeInitVulkan(Object surface);
    public static native void nativeRenderFrame();
    public static native void nativeCleanup();
    public static native void setIndirectDrawEnabled(boolean enabled);
    public static native void setMultiThreadedRendering(boolean enabled);
    public static native void setTargetFPS(int fps);
}
