package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.effect.SeaponyGraceStatusEffect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.GuardianEntity;

@Mixin(targets = "net.minecraft.entity.mob.GuardianEntity$GuardianTargetPredicate")
abstract class MixinGuardianTargetPredicate {
    @Shadow
    private @Final GuardianEntity owner;

    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void unicopia_excludeSeaponysGrace(@Nullable LivingEntity target, CallbackInfoReturnable<Boolean> info) {
        if (!SeaponyGraceStatusEffect.hasIre(target, owner)) {
            info.setReturnValue(false);
        }
    }
}
