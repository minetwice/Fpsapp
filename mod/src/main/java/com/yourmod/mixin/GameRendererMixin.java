package com.yourmod.mixin;

import com.yourmod.PerformanceClient;
import com.yourmod.VulkanManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    private float lastPitch = 0;
    private float lastYaw = 0;
    private long lastFrameTime = 0;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        VulkanManager.getInstance().renderFrame();
        PerformanceClient.applyRealtimeOptimizations();

        // Frame pacing for smooth camera movement
        long now = System.currentTimeMillis();
        if (lastFrameTime != 0) {
            long delta = now - lastFrameTime;
            if (delta > 1000 / 60 + 5) {  // if frame took longer than 60fps baseline
                PerformanceClient.adjustFramePacing(delta);
            }
        }
        lastFrameTime = now;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        // Any post‑frame cleanup
    }

    @Inject(method = "updateCamera", at = @At("HEAD"))
    private void onCameraUpdate(CallbackInfo ci) {
        GameRenderer renderer = (GameRenderer)(Object)this;
        float currentPitch = renderer.getCamera().getPitch();
        float currentYaw = renderer.getCamera().getYaw();
        float deltaX = currentPitch - lastPitch;
        float deltaY = currentYaw - lastYaw;
        if (deltaX != 0 || deltaY != 0) {
            PerformanceClient.onCameraMove(deltaX, deltaY);
            lastPitch = currentPitch;
            lastYaw = currentYaw;
        }
    }
}
