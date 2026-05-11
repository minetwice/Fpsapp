package com.yourmod;

import android.util.Log;

/**
 * The JNI bridge between Java and native Vulkan renderer.
 */
public class VulkanBridge {
    private static final String TAG = "VulkanBridge";

    // 🟢 Load the native library
    static {
        try {
            System.loadLibrary("vulkan_renderer");
            Log.i(TAG, "Native library loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Failed to load native library: " + e.getMessage());
        }
    }

    // 🟢 Native method declarations (implemented in C++)
    public static native boolean nativeInitVulkan(Object surface);
    public static native void nativeRenderFrame();
    public static native void nativeCleanup();
}
