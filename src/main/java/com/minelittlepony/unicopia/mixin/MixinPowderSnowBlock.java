package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;

@Mixin(PowderSnowBlock.class)
abstract class MixinPowderSnowBlock {
    @Inject(method = "canWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
    private static void onCanWalkOnPowderSnow(Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (Pony.of(entity).map(Pony::getSpecies).filter(Race::canFly).isPresent()) {
            info.setReturnValue(true);
        }
    }
}
