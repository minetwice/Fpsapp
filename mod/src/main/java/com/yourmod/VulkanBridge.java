package com.yourmod;

import android.util.Log;

public class VulkanBridge {
    private static final String TAG = "VulkanBridge";

    static {
        System.loadLibrary("vulkan_renderer");
    }

    public static native boolean nativeInitVulkan(Object surface);
    public static native void nativeRenderFrame();
    public static native void nativeCleanup();
}
