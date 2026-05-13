package com.yourapp;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView appList;
    private Button confirmButton;
    private TextView statusText;
    private AppListAdapter adapter;
    private String selectedPackage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appList = findViewById(R.id.appList);
        confirmButton = findViewById(R.id.confirmButton);
        statusText = findViewById(R.id.statusText);

        appList.setLayoutManager(new LinearLayoutManager(this));
        loadInstalledApps();

        confirmButton.setOnClickListener(v -> {
            if (selectedPackage != null) {
                Intent serviceIntent = new Intent(this, PerformanceService.class);
                serviceIntent.putExtra("target_package", selectedPackage);
                startService(serviceIntent);
                statusText.setText("✅ Service Started for: " + selectedPackage);
            } else {
                statusText.setText("⚠️ Please select a launcher first");
            }
        });
    }

    private void loadInstalledApps() {
        PackageManager pm = getPackageManager();
        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<AppInfo> appListData = new ArrayList<>();

        for (ApplicationInfo app : packages) {
            if (pm.getLaunchIntentForPackage(app.packageName) != null) {
                String name = pm.getApplicationLabel(app).toString();
                Drawable icon = app.loadIcon(pm);
                appListData.add(new AppInfo(name, app.packageName, icon));
            }
        }

        adapter = new AppListAdapter(appListData, packageName -> {
            selectedPackage = packageName;
        });
        appList.setAdapter(adapter);
    }

    static class AppInfo {
        String name, packageName;
        Drawable icon;

        AppInfo(String name, String packageName, Drawable icon) {
            this.name = name;
            this.packageName = packageName;
            this.icon = icon;
        }
    }
}
