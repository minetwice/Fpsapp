package com.yourmod.mixin;

import com.yourmod.PerformanceClient;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        // Called at the very beginning of each frame render
        // Your APK can send tuning commands over socket here
        // For now, just a placeholder to show it's being called
        // PerformanceClient.LOGGER.info("Frame rendering started");
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderEnd(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        // Called after frame is rendered, good place for FPS cap / syncing
        // PerformanceClient.LOGGER.info("Frame rendering ended");
    }
}
