package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.ability.magic.spell.effect.HydrophobicSpell;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

@Mixin(FlowableFluid.class)
abstract class MixinFlowableFluid {
    @ModifyReturnValue(method = "canFill(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/fluid/Fluid;)Z", at = @At("RETURN"))
    private static boolean onCanFill(boolean upstreamCheck, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return upstreamCheck && !HydrophobicSpell.blocksFluidFlow(world, pos, fluid.getDefaultState());
    }
}
