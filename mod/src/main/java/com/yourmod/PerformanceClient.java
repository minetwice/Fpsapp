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
    private static boolean enableIndirectDraw = true;
    private static boolean enableMultiThreadedRendering = true;
    private static int targetFPS = 500;

    @Override
    public void onInitializeClient() {
        connectToOptimizerApp();
        applyNativeOptimizations();
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
                        LOGGER.info("Received and applied config: " + configJson);
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
        // Simple parsing - in production use Gson
        if (json.contains("target_package")) {
            targetPackage = json.split("target_package\":\"")[1].split("\"")[0];
        }
        if (json.contains("enable_indirect_draw")) {
            enableIndirectDraw = json.contains("\"enable_indirect_draw\":true");
        }
        if (json.contains("enable_multi_threaded_rendering")) {
            enableMultiThreadedRendering = json.contains("\"enable_multi_threaded_rendering\":true");
        }
        if (json.contains("target_fps")) {
            targetFPS = Integer.parseInt(json.split("target_fps\":")[1].split(",")[0]);
        }
    }

    private void applyNativeOptimizations() {
        System.loadLibrary("vulkan_renderer");
        if (enableIndirectDraw) {
            VulkanBridge.setIndirectDrawEnabled(true);
        }
        if (enableMultiThreadedRendering) {
            VulkanBridge.setMultiThreadedRendering(true);
        }
        VulkanBridge.setTargetFPS(targetFPS);
    }
}
