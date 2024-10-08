package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.block.cloud.CloudLike;

import net.minecraft.block.BlockState;
import net.minecraft.world.Heightmap;

@Mixin(Heightmap.class)
abstract class MixinHeightmap {
    @Inject(method = "method_16682", at = @At("HEAD"), cancellable = true)
    private static void excludeCloudsFromWorldSurfaceHeightMap(BlockState state, CallbackInfoReturnable<Boolean> info) {
        if (state.getBlock() instanceof CloudLike) {
            info.setReturnValue(false);
        }
    }
}
