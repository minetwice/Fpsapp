package com.yourmod.mixin;

import com.yourmod.PerformanceMonitor;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onShouldRender(Entity entity, Vec3d cameraPos, double range, CallbackInfoReturnable<Boolean> cir) {
        double distance = entity.squaredDistanceTo(cameraPos.x, cameraPos.y, cameraPos.z);
        double maxDist = PerformanceMonitor.getEntityCullingDistance();
        if (distance > maxDist * maxDist) {
            cir.setReturnValue(false);
        }
    }
}
