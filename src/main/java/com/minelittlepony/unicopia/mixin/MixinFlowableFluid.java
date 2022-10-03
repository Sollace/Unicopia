package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.ability.magic.spell.effect.HydrophobicSpell;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@Mixin(FlowableFluid.class)
abstract class MixinFlowableFluid {
    @Inject(method = "canFill", at = @At("HEAD"), cancellable = true)
    private void onCanFill(BlockView world, BlockPos pos, BlockState state, Fluid fluid, CallbackInfoReturnable<Boolean> info) {
        if (world instanceof World w && HydrophobicSpell.blocksFluidFlow(w, pos, state, fluid)) {
            info.setReturnValue(false);
        }
    }
}
