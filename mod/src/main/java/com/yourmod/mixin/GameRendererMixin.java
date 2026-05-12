package com.yourmod.mixin;

import com.yourmod.VulkanManager;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // Corrected to match GameRenderer.render(FJZ)V
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        VulkanManager.getInstance().renderFrame();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderEnd(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        // Any post-render logic if needed
    }
}
