package com.yourmod.mixin;

import com.yourmod.PerformanceMonitor;
import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    @ModifyVariable(method = "build", at = @At("HEAD"), ordinal = 0)
    private int adjustRenderDistance(int original) {
        if (PerformanceMonitor.isLagging()) {
            return Math.min(original, 6);
        }
        return original;
    }
}
