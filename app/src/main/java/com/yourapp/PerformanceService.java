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

    @Override
    public void onCreate() {
        super.onCreate();
        startPerformanceServer();
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
                Log.d("PerfService", "Mod: " + line);
            }
        } catch (Exception e) {
            Log.e("PerfService", "Client handler error: " + e.getMessage());
        }
    }

    private String getPerformanceConfig() {
        return "{"
                + "\"cpu_governor\":\"performance\","
                + "\"vm_swappiness\":10,"
                + "\"sched_utilization\":85,"
                + "\"jvm_heap_start\":\"2048M\","
                + "\"jvm_heap_max\":\"4096M\","
                + "\"g1_gc_regions\":32"
                + "}";
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
