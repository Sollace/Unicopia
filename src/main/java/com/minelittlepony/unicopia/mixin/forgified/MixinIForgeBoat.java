package com.minelittlepony.unicopia.mixin.forgified;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.*;
import com.minelittlepony.unicopia.entity.duck.LavaAffine;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.FluidTags;

@Pseudo
@Mixin(targets = "net.minecraftforge.common.extensions.IForgeBoat")
interface MixinIForgeBoat {
    @ModifyVariable(
            method = "canBoatInFluid(Lnet/minecraft/fluid/FluidState;)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true,
            require = 0
    )
    private FluidState modifyFluidState(FluidState incoming) {
        if (this instanceof LavaAffine a && a.isLavaAffine()) {
            if (incoming.isIn(FluidTags.WATER)) {
                return Fluids.LAVA.getDefaultState();
            }
            if (incoming.isIn(FluidTags.LAVA)) {
                return Fluids.WATER.getDefaultState();
            }
        }

        return incoming;
    }
    @SuppressWarnings("deprecation")
    @ModifyVariable(
            method = "canBoatInFluid(Lnet/minecraft/fluid/Fluid;)Z",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true,
            require = 0
    )
    private Fluid modifyFluid(Fluid incoming) {
        if (this instanceof LavaAffine a && a.isLavaAffine()) {
            if (incoming.isIn(FluidTags.WATER)) {
                return Fluids.LAVA;
            }
            if (incoming.isIn(FluidTags.LAVA)) {
                return Fluids.WATER;
            }
        }

        return incoming;
    }
}
