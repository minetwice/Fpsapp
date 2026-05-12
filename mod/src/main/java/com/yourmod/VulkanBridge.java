package com.yourmod;

public class VulkanBridge {
    // No Android imports – use Java logging or Fabric's LOGGER
    private static final String TAG = "VulkanBridge";

    static {
        // Load native library – works in both PC and Android (if .so in JAR's lib/)
        System.loadLibrary("vulkan_renderer");
    }

    public static native boolean nativeInitVulkan(Object surface);
    public static native void nativeRenderFrame();
    public static native void nativeCleanup();
}
