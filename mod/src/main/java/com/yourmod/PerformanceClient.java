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

    @Override
    public void onInitializeClient() {
        connectToOptimizerApp();
    }

    private void connectToOptimizerApp() {
        new Thread(() -> {
            while (true) {
                try (Socket socket = new Socket("127.0.0.1", PORT)) {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String configJson = in.readLine();
                    if (configJson != null) {
                        LOGGER.info("Received config: " + configJson);
                        // Apply optimizations here
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
}
