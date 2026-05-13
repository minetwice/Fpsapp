package com.yourmod.mixin;

import com.yourmod.PerformanceMonitor;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
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
        // Get camera position via render dispatcher's camera field (obfuscated name? Use accessor)
        // For simplicity, we just skip if distance > threshold using entity's position relative to player
        net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
        if (client.player == null) return;
        double distSq = entity.squaredDistanceTo(client.player);
        double maxDist = PerformanceMonitor.getEntityCullingDistance();
        if (distSq > maxDist * maxDist) {
            ci.cancel();
        }
    }
}
