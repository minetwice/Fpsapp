package com.yourmod.mixin;

import com.yourmod.PerformanceMonitor;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
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
        // Get camera position from the render dispatcher
        Vec3d cameraPos = ((EntityRenderDispatcher)(Object)this).camera.getPos();
        double dx = entity.getX() - cameraPos.x;
        double dy = entity.getY() - cameraPos.y;
        double dz = entity.getZ() - cameraPos.z;
        double distanceSq = dx*dx + dy*dy + dz*dz;
        double maxDist = PerformanceMonitor.getEntityCullingDistance();
        if (distanceSq > maxDist * maxDist) {
            ci.cancel();
        }
    }
}
