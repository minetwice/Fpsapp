package com.yourmod;

public class PerformanceMonitor {
    private static int avgFps = 60;

    public static void update(int fps) { avgFps = (avgFps + fps) / 2; }
    public static int getAverageFps() { return avgFps; }
    public static boolean isLagging() { return avgFps < 80; }
    public static double getEntityCullingDistance() { return 32.0; }
}
