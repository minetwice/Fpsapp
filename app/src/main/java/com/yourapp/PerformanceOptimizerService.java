package com.yourapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.zip.*;

public class PerformanceOptimizerService extends Service {
    private static final int PORT = 12346;
    private ServerSocket serverSocket;
    private boolean isRunning = true;
    private String targetPackage = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("OptService", "Performance Optimizer Service Started");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("target_package")) {
            targetPackage = intent.getStringExtra("target_package");
            optimizeGameFiles();
        }
        startJitterReductionServer();
        return START_STICKY;
    }

    private void optimizeGameFiles() {
        try {
            File gameDir = new File("/storage/emulated/0/Android/data/" + targetPackage);
            if (gameDir.exists()) {
                compressBackup(gameDir);
                clearTempFiles(gameDir);
                Log.d("OptService", "Game files optimized for " + targetPackage);
            }
        } catch (Exception e) {
            Log.e("OptService", "Optimization error: " + e.getMessage());
        }
    }

    private void compressBackup(File dir) {
        try {
            File backup = new File(dir.getParentFile(), "backup_" + System.currentTimeMillis() + ".zip");
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(backup));
            zipDir(dir, dir.getName(), zos);
            zos.close();
            Log.d("OptService", "Backup created: " + backup.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OptService", "Backup failed: " + e.getMessage());
        }
    }

    private void zipDir(File dir, String parentPath, ZipOutputStream zos) throws IOException {
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                zipDir(file, parentPath + "/" + file.getName(), zos);
            } else {
                zos.putNextEntry(new ZipEntry(parentPath + "/" + file.getName()));
                FileInputStream fis = new FileInputStream(file);
                byte[] buffer = new byte[8192];
                int length;
                while ((length = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, length);
                }
                fis.close();
                zos.closeEntry();
            }
        }
    }

    private void clearTempFiles(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && (file.getName().endsWith(".tmp") || 
                    file.getName().endsWith(".cache") || file.getName().endsWith(".log"))) {
                    file.delete();
                }
            }
        }
    }

    private void startJitterReductionServer() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(PORT);
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    handleClient(clientSocket);
                }
            } catch (Exception e) { Log.e("OptService", "Server error: " + e.getMessage()); }
        }).start();
    }

    private void handleClient(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            out.println(getOptimizationConfig());
            String line;
            while ((line = in.readLine()) != null) {
                Log.d("OptService", "Mod stats: " + line);
            }
        } catch (Exception e) { Log.e("OptService", "Client error: " + e.getMessage()); }
    }

    private String getOptimizationConfig() {
        return "{"
                + "\"jitter_reduction\":true,"
                + "\"preload_chunks\":true,"
                + "\"smooth_lighting\":false,"
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
