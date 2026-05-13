package com.yourmod;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.*;
import java.net.Socket;

public class PerformanceClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("VulkanOptimizer");
    private static final int PORT = 12345;
    private static String targetPackage = "";
    private static boolean optimizeEntities = true;
    private static boolean optimizeBlocks = true;
    private static boolean optimizeHits = true;
    private static boolean optimizeCamera = true;
    private static boolean fixReplayLag = true;
    private static int targetFPS = 500;
    private static boolean replayActive = false;
    private static boolean multiThreading = true;
    private static boolean dynamicResolution = true;
    private static boolean smartCulling = true;

    @Override
    public void onInitializeClient() {
        LOGGER.info("PerformanceClient initializing...");
        connectToOptimizerApp();
        applyNativeSettings();
    }

    private void connectToOptimizerApp() {
        new Thread(() -> {
            while (true) {
                try (Socket socket = new Socket("127.0.0.1", PORT)) {
                    LOGGER.info("Connected to APK performance service");
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                    String configJson = in.readLine();
                    if (configJson != null) {
                        parseConfig(configJson);
                        applyNativeSettings();
                        LOGGER.info("Applied config: " + configJson);
                    }
                    
                    while (true) {
                        int fps = MinecraftClient.getInstance().getCurrentFps();
                        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                        out.println("{\"fps\":" + fps + ",\"mem_used\":" + mem + "}");
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    LOGGER.warn("APK connection failed (make sure app is running): " + e.getMessage());
                    try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                }
            }
        }).start();
    }

    private void parseConfig(String json) {
        if (json.contains("optimize_entities")) optimizeEntities = json.contains("\"optimize_entities\":true");
        if (json.contains("optimize_blocks")) optimizeBlocks = json.contains("\"optimize_blocks\":true");
        if (json.contains("optimize_hits")) optimizeHits = json.contains("\"optimize_hits\":true");
        if (json.contains("optimize_camera")) optimizeCamera = json.contains("\"optimize_camera\":true");
        if (json.contains("fix_replay_lag")) fixReplayLag = json.contains("\"fix_replay_lag\":true");
        if (json.contains("target_fps")) targetFPS = Integer.parseInt(json.split("target_fps\":")[1].split(",")[0]);
        if (json.contains("multi_threading")) multiThreading = json.contains("\"multi_threading\":true");
        if (json.contains("dynamic_resolution")) dynamicResolution = json.contains("\"dynamic_resolution\":true");
        if (json.contains("smart_culling")) smartCulling = json.contains("\"smart_culling\":true");
    }

    public static void enablePVPMode(boolean enable) {
    com.yourmod.pvp.PVPOptimizer.setEnabled(enable);
    if (enable) {
        LOGGER.info("PVP Optimization Mode ENABLED - lag reduction active");
    } else {
        LOGGER.info("PVP Optimization Mode DISABLED");
    }
}

public static void setHitLagReduction(boolean enable) {
    com.yourmod.pvp.PVPOptimizer.setHitLagReduction(enable);
}

public static void setKnockbackOptimization(boolean enable) {
    com.yourmod.pvp.PVPOptimizer.setKnockbackOptimization(enable);
}

public static void setCritLagFix(boolean enable) {
    com.yourmod.pvp.PVPOptimizer.setCritLagFix(enable);
}

    private void applyNativeSettings() {
        try {
            VulkanBridge.setOptimizationFlags(optimizeEntities, optimizeBlocks, optimizeHits, optimizeCamera, fixReplayLag, true);
            VulkanBridge.setTargetFPS(targetFPS);
            VulkanBridge.enableMultiThreading(multiThreading);
            VulkanBridge.enableDynamicResolution(dynamicResolution);
            LOGGER.info("Native optimizations applied");
        } catch (UnsatisfiedLinkError e) {
            LOGGER.warn("Native method not available: " + e.getMessage());
        }
    }

    public static void applyRealtimeOptimizations() {
        try {
            VulkanBridge.applyRealtimeOptimizations();
        } catch (UnsatisfiedLinkError e) { /* ignore */ }
    }

    public static void adjustFramePacing(long frameDelta) {
        if (targetFPS > 200 && frameDelta > 1000 / targetFPS + 5) {
            LOGGER.debug("Frame pacing adjusted");
        }
    }

    public static void onCameraMove(float deltaX, float deltaY) {
        if (optimizeCamera) {
            try { VulkanBridge.onCameraMove(deltaX, deltaY); } catch (UnsatisfiedLinkError e) {}
        }
    }

    public static void setReplayMode(boolean active) {
        replayActive = active;
        if (fixReplayLag) LOGGER.debug("Replay mode: " + active);
    }

    public static boolean isReplayActive() { return replayActive; }

    public static boolean isSmartCullingEnabled() { return smartCulling; }
}
