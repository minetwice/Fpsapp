package com.yourapp;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class PerformanceService extends Service {
    private static final int PORT = 12345;
    private ServerSocket serverSocket;
    private boolean isRunning = true;
    private String targetPackage = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("PerfService", "Performance Booster Service Started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("target_package")) {
            targetPackage = intent.getStringExtra("target_package");
            clearGameCache(); // Clear cache of selected launcher
        }
        startPerformanceServer();
        return START_STICKY;
    }

    private void clearGameCache() {
        try {
            // Open the app's settings page so user can manually clear cache (no root needed)
            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(android.net.Uri.parse("package:" + targetPackage));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            Log.d("PerfService", "Opened cache settings for " + targetPackage);
        } catch (Exception e) {
            Log.e("PerfService", "Could not open cache settings: " + e.getMessage());
        }
    }

    private void startPerformanceServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                }
            } catch (Exception e) { Log.e("PerfService", "Server error: " + e.getMessage()); }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            out.println(getPerformanceConfig());
            String line;
            while ((line = in.readLine()) != null) {
                Log.d("PerfService", "Mod stats: " + line);
                // dynamic tuning based on FPS can be added here
            }
        } catch (Exception e) { Log.e("PerfService", "Client error: " + e.getMessage()); }
    }

    private String getPerformanceConfig() {
        return "{"
                + "\"optimize_entities\":true,"
                + "\"optimize_hits\":true,"
                + "\"optimize_camera\":true,"
                + "\"dynamic_resolution\":true,"
                + "\"multi_threading\":true,"
                + "\"smart_culling\":true,"
                + "\"target_fps\":500"
                + "}";
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        isRunning = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception e) {}
        super.onDestroy();
    }
}
