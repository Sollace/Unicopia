package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.gas.Gas;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;

@Mixin(FallingBlock.class)
abstract class MixinFallingBlock extends Block {
    MixinFallingBlock() { super(null); }
    @Inject(method = "canFallThrough(Lnet/minecraft/block/BlockState;)Z",
            at = @At("HEAD"),
            cancellable = true)
    private static void onCanFallThrough(BlockState state, CallbackInfoReturnable<Boolean> info) {
        if (state.getBlock() instanceof Gas) {
            info.setReturnValue(!((Gas)state.getBlock()).isSupporting(state));
        }
    }
}
