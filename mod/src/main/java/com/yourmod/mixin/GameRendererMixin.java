package com.yourmod.mixin;

import com.yourmod.VulkanManager;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.profiler.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // Updated signature for Minecraft 1.21.11
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        // Called at the very beginning of each frame render
        VulkanManager.getInstance().renderFrame();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        // Called after frame is rendered
        // You can add cleanup or FPS pacing here
    }
}
