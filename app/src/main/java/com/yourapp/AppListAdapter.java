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

    private List<MainActivity.AppInfo> gameList;
    private OnGameSelectedListener listener;
    private int selectedPosition = -1;

    public interface OnGameSelectedListener {
        void onGameSelected(String packageName);
    }

    public AppListAdapter(List<MainActivity.AppInfo> gameList, OnGameSelectedListener listener) {
        this.gameList = gameList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MainActivity.AppInfo game = gameList.get(position);
        holder.gameName.setText(game.name);
        holder.gamePackage.setText(game.packageName);
        holder.gameIcon.setImageDrawable(game.icon);
        holder.radioButton.setChecked(position == selectedPosition);

        holder.itemView.setOnClickListener(v -> {
            selectedPosition = position;
            notifyDataSetChanged();
            listener.onGameSelected(game.packageName);
        });
    }

    @Override
    public int getItemCount() { return gameList.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView gameName, gamePackage;
        ImageView gameIcon;
        RadioButton radioButton;
        ViewHolder(View itemView) {
            super(itemView);
            gameName = itemView.findViewById(R.id.gameName);
            gamePackage = itemView.findViewById(R.id.gamePackage);
            gameIcon = itemView.findViewById(R.id.gameIcon);
            radioButton = itemView.findViewById(R.id.radioButton);
        }
    }
}
