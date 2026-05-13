package com.yourmod.pvp;

import com.yourmod.PerformanceMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class PVPOptimizer {
    private static final Logger LOGGER = LoggerFactory.getLogger("PVPOptimizer");
    private static boolean enabled = true;
    private static boolean hitLagReduction = true;
    private static boolean knockbackOptimization = true;
    private static boolean critLagFix = true;
    private static ConcurrentHashMap<Integer, Long> hitCooldownMap = new ConcurrentHashMap<>();

    public static void init() {
        enabled = true;
        LOGGER.info("PVP Optimizer initialized");
    }

    public static void onPlayerHit(PlayerEntity player, Entity attacker, float damage) {
        if (!enabled || !hitLagReduction) return;
        int id = player.getId();
        long now = System.currentTimeMillis();
        Long last = hitCooldownMap.get(id);
        if (last == null || (now - last) > 50) {
            hitCooldownMap.put(id, now);
        }
    }

    public static void onKnockbackPre() {
        if (knockbackOptimization) {
            Thread.yield();
        }
    }

    public static void onCrit() {
        if (critLagFix) {
            // Pre-calc next frame (stub)
        }
    }

    // Optimized entity search for hit detection
    public static Entity getNearestEntityForHit(MinecraftClient client, double reach) {
        if (!enabled) return null;
        Entity cameraEntity = client.getCameraEntity();
        if (cameraEntity == null) return null;
        Vec3d cameraPos = cameraEntity.getEyePos();
        double maxDistSq = reach * reach;
        Entity closest = null;
        double closestDistSq = maxDistSq;
        
        for (Entity entity : client.world.getEntities()) {
            if (entity == cameraEntity) continue;
            if (!entity.isAttackable()) continue;
            Box box = entity.getBoundingBox().expand(0.1);
            // Compute closest point on box to camera position
            double dx = Math.max(box.minX - cameraPos.x, 0) + Math.max(cameraPos.x - box.maxX, 0);
            double dy = Math.max(box.minY - cameraPos.y, 0) + Math.max(cameraPos.y - box.maxY, 0);
            double dz = Math.max(box.minZ - cameraPos.z, 0) + Math.max(cameraPos.z - box.maxZ, 0);
            double distSq = dx*dx + dy*dy + dz*dz;
            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = entity;
            }
        }
        return closest;
    }

    public static void optimizeKnockback(LivingEntity entity, double x, double z) {
        if (!knockbackOptimization) return;
        entity.setVelocity(entity.getVelocity().add(x * 0.4, 0.0, z * 0.4));
    }

    public static void onDamageComplete() {
        if (hitLagReduction && hitCooldownMap.size() > 100) {
            hitCooldownMap.clear();
        }
    }

    public static void setEnabled(boolean enable) { enabled = enable; }
    public static void setHitLagReduction(boolean enable) { hitLagReduction = enable; }
    public static void setKnockbackOptimization(boolean enable) { knockbackOptimization = enable; }
    public static void setCritLagFix(boolean enable) { critLagFix = enable; }
    public static boolean isEnabled() { return enabled; }
}
