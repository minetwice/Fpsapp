package com.yourmod;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

public class PerformanceMonitor {
    private static final int SAMPLE_SIZE = 20;
    private static final int[] fpsHistory = new int[SAMPLE_SIZE];
    private static int index = 0;
    private static int total = 0;
    private static int avgFps = 60;
    private static int lastFrameTime = 0;

    public static void update(int currentFps) {
        total -= fpsHistory[index];
        fpsHistory[index] = currentFps;
        total += currentFps;
        index = (index + 1) % SAMPLE_SIZE;
        avgFps = total / SAMPLE_SIZE;
    }

    public static int getAverageFps() { return avgFps; }

    public static boolean isLagging() { return avgFps < 80; }

    public static float getDynamicRenderDistance() {
        // Reduce render distance if FPS drops below 70
        if (avgFps < 60) return 6;
        if (avgFps < 80) return 8;
        if (avgFps < 120) return 12;
        return 16;
    }

    public static float getEntityCullingDistance() {
        if (avgFps < 60) return 16;
        if (avgFps < 90) return 24;
        return 32;
    }

    public static void applyDynamicSettings() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.options != null) {
            float newRenderDist = getDynamicRenderDistance();
            if (client.options.getViewDistance().getValue() != (int)newRenderDist) {
                client.options.getViewDistance().setValue((int)newRenderDist);
                client.worldRenderer.reload();
            }
        }
    }
}
