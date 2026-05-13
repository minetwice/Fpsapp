package com.yourapp;

import android.app.Service;
import android.content.Intent;
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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("target_package")) {
            targetPackage = intent.getStringExtra("target_package");
            Log.d("PerfService", "Target package: " + targetPackage);
        }
        startPerformanceServer();
        return START_STICKY;
    }

    private void startPerformanceServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                }
            } catch (Exception e) {
                Log.e("PerfService", "Server error: " + e.getMessage());
            }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            out.println(getPerformanceConfig());
            String line;
            while ((line = in.readLine()) != null) {
                Log.d("PerfService", "Mod data: " + line);
            }
        } catch (Exception e) {
            Log.e("PerfService", "Client handler error: " + e.getMessage());
        }
    }

    private String getPerformanceConfig() {
        return "{"
                + "\"target_package\":\"" + targetPackage + "\","
                + "\"optimize_entities\":true,"
                + "\"optimize_blocks\":true,"
                + "\"optimize_hits\":true,"
                + "\"optimize_camera\":true,"
                + "\"fix_replay_lag\":true,"
                + "\"cpu_governor\":\"performance\","
                + "\"multi_threading\":true,"
                + "\"dynamic_resolution\":true,"
                + "\"jvm_heap_start\":\"4096M\","
                + "\"jvm_heap_max\":\"8192M\","
                + "\"target_fps\":500"
                + "}";
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        isRunning = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception e) { }
        super.onDestroy();
    }
}
