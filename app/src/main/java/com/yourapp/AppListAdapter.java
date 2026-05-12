package com.yourapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    private List<MainActivity.AppInfo> appList;
    private OnAppSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnAppSelectedListener {
        void onAppSelected(String packageName);
    }

    public AppListAdapter(List<MainActivity.AppInfo> appList, OnAppSelectedListener listener) {
        this.appList = appList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.AppInfo app = appList.get(position);
        holder.appName.setText(app.name);
        holder.appPackage.setText(app.packageName);
        holder.appIcon.setImageDrawable(app.icon);
        holder.radioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();
            listener.onAppSelected(app.packageName);
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView appName, appPackage;
        ImageView appIcon;
        RadioButton radioButton;

        ViewHolder(View itemView) {
            super(itemView);
            appName = itemView.findViewById(R.id.appName);
            appPackage = itemView.findViewById(R.id.appPackage);
            appIcon = itemView.findViewById(R.id.appIcon);
            radioButton = itemView.findViewById(R.id.radioButton);
        }
    }
}
