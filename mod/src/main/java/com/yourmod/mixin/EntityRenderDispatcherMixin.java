package com.yourmod.mixin;

import com.yourmod.PerformanceMonitor;
import com.yourmod.pvp.PVPOptimizer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void onRender(E entity, double x, double y, double z, float yaw, float tickDelta,
                                             MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
                                             CallbackInfo ci) {
        // During combat, cull distant entities more aggressively
        if (PVPOptimizer.isEnabled()) {
            double distSq = entity.squaredDistanceTo(entity.getWorld().getClosestPlayer(entity, 64.0));
            double maxDist = PerformanceMonitor.getEntityCullingDistance();
            if (distSq > maxDist * maxDist) {
                ci.cancel();
            }
        }
    }
}
