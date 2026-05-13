package com.yourmod;

import net.minecraft.client.MinecraftClient;

public class PerformanceMonitor {
    private static final int SAMPLE_SIZE = 20;
    private static final int[] fpsHistory = new int[SAMPLE_SIZE];
    private static int index = 0;
    private static int total = 0;
    private static int avgFps = 60;
    private static long lastFrameTime = 0;
    private static int currentFps = 60;

    public static void update(int fps) {
        currentFps = fps;
        total -= fpsHistory[index];
        fpsHistory[index] = fps;
        total += fps;
        index = (index + 1) % SAMPLE_SIZE;
        avgFps = total / SAMPLE_SIZE;
    }

    public static int getCurrentFps() {
        return currentFps;
    }

    public static int getAverageFps() {
        return avgFps;
    }

    public static boolean isLagging() {
        return avgFps < 80;
    }

    public static double getEntityCullingDistance() {
        if (avgFps < 60) return 16.0;
        if (avgFps < 120) return 32.0;
        return 64.0;
    }

    public static float getDynamicRenderDistance() {
        if (avgFps < 60) return 6;
        if (avgFps < 80) return 8;
        if (avgFps < 120) return 12;
        return 16;
    }

    public static void applyDynamicSettings() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world != null && client.options != null) {
            int newRenderDist = (int) getDynamicRenderDistance();
            if (client.options.getViewDistance().getValue() != newRenderDist) {
                client.options.getViewDistance().setValue(newRenderDist);
                client.worldRenderer.reload();
            }
        }
    }
}
