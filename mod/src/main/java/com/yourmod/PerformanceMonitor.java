package com.yourmod;

public class PerformanceMonitor {
    private static final int SAMPLE_SIZE = 20;
    private static final int[] fpsHistory = new int[SAMPLE_SIZE];
    private static int index = 0;
    private static int total = 0;
    private static int avgFps = 60;

    public static void update(int currentFps) {
        total -= fpsHistory[index];
        fpsHistory[index] = currentFps;
        total += currentFps;
        index = (index + 1) % SAMPLE_SIZE;
        avgFps = total / SAMPLE_SIZE;
    }

    public static int getAverageFps() { return avgFps; }
    public static boolean isLagging() { return avgFps < 80; }
    public static double getEntityCullingDistance() {
        if (avgFps < 60) return 16.0;
        if (avgFps < 120) return 32.0;
        return 64.0;
    }
}
