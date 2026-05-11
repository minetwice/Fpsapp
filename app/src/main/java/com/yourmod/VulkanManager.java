package com.yourmod;

import android.util.Log;
import android.view.Surface;

/**
 * Manages the Vulkan renderer lifecycle and interactions.
 */
public class VulkanManager {
    private static final String TAG = "VulkanManager";
    private static VulkanManager instance;
    private boolean isInitialized = false;

    public static synchronized VulkanManager getInstance() {
        if (instance == null) {
            instance = new VulkanManager();
        }
        return instance;
    }

    /**
     * 🟡 Initializes the Vulkan renderer with a Surface.
     * @param surface The Android surface to render to (from launcher)
     * @return true if initialization succeeded
     */
    public boolean initialize(Surface surface) {
        if (isInitialized) {
            Log.w(TAG, "Already initialized");
            return true;
        }

        if (surface == null || !surface.isValid()) {
            Log.e(TAG, "Invalid surface provided");
            return false;
        }

        try {
            Log.i(TAG, "Calling nativeInitVulkan...");
            boolean success = VulkanBridge.nativeInitVulkan(surface);
            if (success) {
                isInitialized = true;
                Log.i(TAG, "Vulkan renderer initialized successfully!");
            } else {
                Log.e(TAG, "Vulkan initialization failed");
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Exception during initialization: " + e.getMessage());
            return false;
        }
    }

    /**
     * 🟡 Renders the current frame.
     */
    public void renderFrame() {
        if (!isInitialized) {
            Log.w(TAG, "Cannot render, not initialized");
            return;
        }
        VulkanBridge.nativeRenderFrame();
    }

    /**
     * 🟡 Cleans up Vulkan resources.
     */
    public void cleanup() {
        if (isInitialized) {
            VulkanBridge.nativeCleanup();
            isInitialized = false;
            Log.i(TAG, "Vulkan renderer cleaned up");
        }
    }
}
