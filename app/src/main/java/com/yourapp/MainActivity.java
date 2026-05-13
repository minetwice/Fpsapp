package com.yourapp;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView gameList;
    private Button confirmButton;
    private TextView statusText;
    private AppListAdapter adapter;
    private String selectedPackage = null;

    // List of known Minecraft Java launchers (add more as needed)
    private static final String[] GAME_LAUNCHERS = {
        "git.artdeell.mojo",
        "net.kdt.pojavlaunch",
        "com.foldcraft.launcher",
        "com.zackpm.ps1",
        "com.mojang.minecraftpe"  // for Bedrock, optional
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameList = findViewById(R.id.gameList);
        confirmButton = findViewById(R.id.confirmButton);
        statusText = findViewById(R.id.statusText);

        gameList.setLayoutManager(new LinearLayoutManager(this));
        loadGameLaunchersOnly();

        confirmButton.setOnClickListener(v -> {
            if (selectedPackage != null) {
                Intent serviceIntent = new Intent(this, PerformanceService.class);
                serviceIntent.putExtra("target_package", selectedPackage);
                startService(serviceIntent);
                statusText.setText("✅ Boosting: " + selectedPackage);
            } else {
                statusText.setText("⚠️ Select a game launcher first");
            }
        });
    }

    private void loadGameLaunchersOnly() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> allApps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<AppInfo> gameListData = new ArrayList<>();

        for (String launchPackage : GAME_LAUNCHERS) {
            try {
                ApplicationInfo app = pm.getApplicationInfo(launchPackage, 0);
                String name = pm.getApplicationLabel(app).toString();
                Drawable icon = app.loadIcon(pm);
                gameListData.add(new AppInfo(name, launchPackage, icon));
            } catch (PackageManager.NameNotFoundException ignored) {
                // Not installed
            }
        }

        // Also add any other app that has launcher intent and contains "minecraft" or "launcher"
        for (ApplicationInfo app : allApps) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                String name = app.packageName.toLowerCase();
                if ((name.contains("minecraft") || name.contains("launcher") || name.contains("mojo") || name.contains("pojav")) &&
                    !gameListData.stream().anyMatch(g -> g.packageName.equals(app.packageName))) {
                    gameListData.add(new AppInfo(pm.getApplicationLabel(app).toString(), app.packageName, app.loadIcon(pm)));
                }
            }
        }

        adapter = new AppListAdapter(gameListData, pkg -> selectedPackage = pkg);
        gameList.setAdapter(adapter);
    }

    static class AppInfo {
        String name, packageName;
        Drawable icon;
        AppInfo(String name, String pkg, Drawable icon) {
            this.name = name;
            this.packageName = pkg;
            this.icon = icon;
        }
    }
}
