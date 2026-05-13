package com.yourmod.mixin;

import com.yourmod.PerformanceClient;
import com.yourmod.PerformanceMonitor;
import com.yourmod.VulkanManager;
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

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        VulkanManager.getInstance().renderFrame();
        PerformanceClient.applyRealtimeOptimizations();

        long now = System.currentTimeMillis();
        if (lastFrameTime != 0) {
            long delta = now - lastFrameTime;
            if (delta > 1000 / 60 + 5) {
                PerformanceClient.adjustFramePacing(delta);
            }
        }
        lastFrameTime = now;
        
        frameCount++;
        if (now - lastLogTime >= 1000) {
            PerformanceMonitor.update((int)(frameCount * 1000 / (now - lastLogTime)));
            frameCount = 0;
            lastLogTime = now;
        }
    }
}
