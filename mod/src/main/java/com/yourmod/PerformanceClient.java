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

    @Override
    public void onInitializeClient() {
        connectToOptimizerApp();
        loadNativeOptimizations();
    }

    private void connectToOptimizerApp() {
        new Thread(() -> {
            while (true) {
                try (Socket socket = new Socket("127.0.0.1", PORT)) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    
                    String configJson = in.readLine();
                    if (configJson != null) {
                        parseConfig(configJson);
                        applyNativeOptimizations();
                        LOGGER.info("Optimization config applied: " + configJson);
                    }
                    
                    while (true) {
                        int fps = MinecraftClient.getInstance().getCurrentFps();
                        long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                        out.println("{\"fps\":" + fps + ",\"mem_used\":" + mem + "}");
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    LOGGER.warn("Connection lost: " + e.getMessage());
                    try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
                }
            }
        }).start();
    }

    private void parseConfig(String json) {
        if (json.contains("optimize_entities")) {
            optimizeEntities = json.contains("\"optimize_entities\":true");
        }
        if (json.contains("optimize_blocks")) {
            optimizeBlocks = json.contains("\"optimize_blocks\":true");
        }
        if (json.contains("optimize_hits")) {
            optimizeHits = json.contains("\"optimize_hits\":true");
        }
        if (json.contains("optimize_camera")) {
            optimizeCamera = json.contains("\"optimize_camera\":true");
        }
        if (json.contains("fix_replay_lag")) {
            fixReplayLag = json.contains("\"fix_replay_lag\":true");
        }
        if (json.contains("target_fps")) {
            targetFPS = Integer.parseInt(json.split("target_fps\":")[1].split(",")[0]);
        }
    }

    private void loadNativeOptimizations() {
        try {
            System.loadLibrary("vulkan_renderer");
        } catch (UnsatisfiedLinkError e) {
            LOGGER.error("Could not load vulkan_renderer: " + e.getMessage());
        }
        applyNativeOptimizations();
    }

    private void applyNativeOptimizations() {
        try {
            VulkanBridge.setOptimizationFlags(optimizeEntities, optimizeBlocks, optimizeHits, optimizeCamera, fixReplayLag, true);
            VulkanBridge.setTargetFPS(targetFPS);
        } catch (UnsatisfiedLinkError e) {
            LOGGER.warn("Native optimization methods not available: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Static methods called from mixins (with exception safety)
    // -------------------------------------------------------------------------
    public static void applyRealtimeOptimizations() {
        try {
            VulkanBridge.applyRealtimeOptimizations();
        } catch (UnsatisfiedLinkError e) {
            LOGGER.debug("applyRealtimeOptimizations not available: " + e.getMessage());
        }
    }

    public static void adjustFramePacing(long frameDelta) {
        if (targetFPS > 200 && frameDelta > 1000 / targetFPS + 5) {
            LOGGER.debug("Frame pacing adjusted: " + frameDelta);
        }
        // No native call needed here – just logging
    }

    public static void onCameraMove(float deltaX, float deltaY) {
        if (optimizeCamera) {
            try {
                VulkanBridge.onCameraMove(deltaX, deltaY);
            } catch (UnsatisfiedLinkError e) {
                LOGGER.debug("onCameraMove not available");
            }
        }
    }

    public static void setReplayMode(boolean active) {
        replayActive = active;
        if (fixReplayLag) {
            LOGGER.debug("Replay mode set to: " + active);
        }
    }

    public static boolean isReplayActive() {
        return replayActive;
    }

    public static void onBlockPlace() {
        if (optimizeBlocks) {
            try {
                VulkanBridge.onBlockPlaceEvent();
            } catch (UnsatisfiedLinkError e) {
                LOGGER.debug("onBlockPlaceEvent not available");
            }
        }
    }

    public static void onHit() {
        if (optimizeHits) {
            try {
                VulkanBridge.onHitEvent();
            } catch (UnsatisfiedLinkError e) {
                LOGGER.debug("onHitEvent not available");
            }
        }
    }
}
