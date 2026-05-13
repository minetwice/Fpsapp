package com.yourmod.mixin;

import com.yourmod.pvp.PVPOptimizer;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "takeKnockback", at = @At("HEAD"))
    private void onKnockbackHead(double strength, double x, double z, CallbackInfo ci) {
        PVPOptimizer.onKnockbackPre();
        PVPOptimizer.optimizeKnockback((LivingEntity)(Object)this, x, z);
    }

    @Inject(method = "crit", at = @At("HEAD"))
    private void onCrit(CallbackInfo ci) {
        PVPOptimizer.onCrit();
    }
}
