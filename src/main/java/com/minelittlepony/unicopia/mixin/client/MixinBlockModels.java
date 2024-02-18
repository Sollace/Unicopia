package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.block.FruitBearingBlock;
import com.minelittlepony.unicopia.compat.sodium.SodiumDelegate;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.BlockModels;

@Mixin(BlockModels.class)
abstract class MixinBlockModels {
    @ModifyVariable(
        method = "getModel(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/model/BakedModel;",
        at = @At("HEAD"),
        argsOnly = true,
        ordinal = 0
    )
    private BlockState getAppearance(BlockState state) {
        if (state.contains(FruitBearingBlock.STAGE)
                && !SodiumDelegate.getInstance().isFancyLeavesOrBetter().orElse(MinecraftClient.isFancyGraphicsOrBetter())) {
            return state.with(FruitBearingBlock.STAGE, FruitBearingBlock.Stage.IDLE);
        }
        return state;
    }
}
