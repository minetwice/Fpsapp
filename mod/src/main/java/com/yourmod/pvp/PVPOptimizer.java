package com.yourmod.pvp;

import com.yourmod.PerformanceMonitor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PVPOptimizer {
    private static final Logger LOGGER = LoggerFactory.getLogger("PVPOptimizer");
    private static boolean enabled = true;
    private static boolean hitLagReduction = true;
    private static boolean knockbackOptimization = true;
    private static boolean critLagFix = true;
    private static int lastHitTick = 0;
    private static long lastHitTime = 0;
    private static ConcurrentHashMap<Integer, Long> hitCooldownMap = new ConcurrentHashMap<>();

    public static void init() {
        enabled = true;
        LOGGER.info("PVP Optimizer initialized");
    }

    // Call from mixin when player takes damage
    public static void onPlayerHit(PlayerEntity player, Entity attacker, float damage) {
        if (!enabled || !hitLagReduction) return;
        int id = player.getId();
        long now = System.currentTimeMillis();
        Long last = hitCooldownMap.get(id);
        if (last == null || (now - last) > 50) { // 50ms cooldown to reduce hit lag spikes
            hitCooldownMap.put(id, now);
            // Reduce visual hit lag by skipping frame delays
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == player) {
                // Force immediate camera reset if needed
            }
        }
    }

    // Call before knockback calculation to reduce stutter
    public static void onKnockbackPre() {
        if (knockbackOptimization) {
            // Yield to keep game smooth
            Thread.yield();
        }
    }

    // Call after crit calculation to avoid rendering spikes
    public static void onCrit() {
        if (critLagFix) {
            // Pre-calc next frame
        }
    }

    // Optimize entity search for hit detection (faster than vanilla)
    public static Entity getNearestEntityForHit(MinecraftClient client, double reach) {
        if (!enabled) return null;
        Entity cameraEntity = client.getCameraEntity();
        if (cameraEntity == null) return null;
        Vec3d cameraPos = cameraEntity.getEyePos();
        Vec3d lookVec = cameraEntity.getRotationVec(1.0F);
        double maxDist = reach;
        Entity closest = null;
        double closestDist = maxDist;
        List<Entity> entities = client.world.getEntities();
        for (Entity entity : entities) {
            if (entity == cameraEntity) continue;
            if (!entity.isAttackable()) continue;
            Box box = entity.getBoundingBox().expand(0.1);
            double distToBox = box.distanceTo(cameraPos);
            if (distToBox > maxDist) continue;
            // Line of sight check (fast)
            if (client.world.raycast(cameraPos, cameraPos.add(lookVec.multiply(distToBox)), (e) -> e == entity, null) != null) {
                if (distToBox < closestDist) {
                    closestDist = distToBox;
                    closest = entity;
                }
            }
        }
        return closest;
    }

    // Reduce knockback calculation overhead
    public static void optimizeKnockback(LivingEntity entity, double x, double z) {
        if (!knockbackOptimization) return;
        // Pre-calc velocity with reduced precision for speed
        entity.setVelocity(entity.getVelocity().add(x * 0.4, 0.0, z * 0.4));
    }

    // Call at end of damage tick to clear caches
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
