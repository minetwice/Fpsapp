package com.yourmod.tick;

import com.yourmod.PerformanceMonitor;
import net.minecraft.entity.Entity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EntityTickScheduler {
    private static final Map<Entity, Integer> tickSkipCounter = new ConcurrentHashMap<>();
    private static final int MAX_TRACK_DISTANCE = 64;
    private static boolean enabled = true;
    private static int currentTickSkip = 0;

    public static void init() {
        enabled = true;
        currentTickSkip = 0;
        tickSkipCounter.clear();
    }

    public static boolean shouldTickEntity(Entity entity) {
        if (!enabled) return true;
        if (entity == null) return true;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return true;
        if (entity == client.player) return true;
        double distSq = entity.squaredDistanceTo(client.player);
        if (distSq > MAX_TRACK_DISTANCE * MAX_TRACK_DISTANCE) return false;
        if (isEntityVisible(entity)) {
            tickSkipCounter.remove(entity);
            return true;
        }
        int skip = tickSkipCounter.getOrDefault(entity, 0);
        if (skip >= currentTickSkip) {
            tickSkipCounter.put(entity, 0);
            return true;
        } else {
            tickSkipCounter.put(entity, skip + 1);
            return false;
        }
    }

    private static boolean isEntityVisible(Entity entity) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return true;
        Box entityBox = entity.getBoundingBox();
        BlockPos blockPos = BlockPos.ofFloored(entityBox.minX, entityBox.minY, entityBox.minZ);
        return client.world.isSpaceEmpty(entityBox) && !client.world.isOutOfHeightLimit(blockPos);
    }

    public static void updateTickRate() {
        int avgFps = PerformanceMonitor.getAverageFps();
        if (avgFps < 30) {
            currentTickSkip = 3;
        } else if (avgFps < 60) {
            currentTickSkip = 1;
        } else {
            currentTickSkip = 0;
        }
    }

    public static void setEnabled(boolean enable) { enabled = enable; }
    public static boolean isEnabled() { return enabled; }
    public static int getTickSkip() { return currentTickSkip; }
}
