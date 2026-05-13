package com.yourmod.mixin;

import com.yourmod.PerformanceClient;
import com.yourmod.VulkanManager;
import com.yourmod.render.GPUDrivenRenderer;
import com.yourmod.tick.EntityTickScheduler;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    private long lastFrameTime = 0;
    private long frameCount = 0;
    private long lastLogTime = 0;
    private static final long TARGET_FRAME_TIME_NS = 1000000000 / 500; // 2ms for 500 FPS

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        long startTime = System.nanoTime();
        VulkanManager.getInstance().renderFrame();
        PerformanceClient.applyRealtimeOptimizations();
        EntityTickScheduler.updateTickRate();
        if (GPUDrivenRenderer.isInitialized()) {
            GPUDrivenRenderer.beginFrame();
        }
        long now = System.currentTimeMillis();
        if (lastFrameTime != 0) {
            long delta = now - lastFrameTime;
            if (delta > 1000 / 60 + 5) {
                PerformanceClient.adjustFramePacing(delta);
            }
        }
        lastFrameTime = now;
        long elapsed = System.nanoTime() - startTime;
        if (elapsed < TARGET_FRAME_TIME_NS) {
            try {
                Thread.sleep((TARGET_FRAME_TIME_NS - elapsed) / 1000000, 
                            (int)((TARGET_FRAME_TIME_NS - elapsed) % 1000000));
            } catch (InterruptedException e) {}
        }
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (GPUDrivenRenderer.isInitialized()) {
            GPUDrivenRenderer.endFrame();
        }
        frameCount++;
        long now = System.currentTimeMillis();
        if (now - lastLogTime >= 1000) {
            PerformanceMonitor.update((int)(frameCount * 1000 / (now - lastLogTime)));
            frameCount = 0;
            lastLogTime = now;
        }
    }
}
