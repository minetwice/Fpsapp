package com.yourmod.mixin;

import com.yourmod.pvp.PVPOptimizer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamagePre(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = source.getAttacker();
        if (attacker != null) {
            PVPOptimizer.onPlayerHit((ClientPlayerEntity)(Object)this, attacker, amount);
        }
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void onDamagePost(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PVPOptimizer.onDamageComplete();
    }

    // Faster hit detection: replace vanilla raycast with optimized version
    @Inject(method = "getTarget", at = @At("HEAD"), cancellable = true)
    private void onGetTarget(double maxDistance, float tickDelta, CallbackInfoReturnable<Entity> cir) {
        if (PVPOptimizer.isEnabled()) {
            Entity target = PVPOptimizer.getNearestEntityForHit((ClientPlayerEntity)(Object)this.client, maxDistance);
            if (target != null) {
                cir.setReturnValue(target);
            }
        }
    }
}
