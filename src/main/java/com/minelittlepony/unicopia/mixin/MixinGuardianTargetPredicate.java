package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.EquinePredicates;

import net.minecraft.entity.LivingEntity;

@Mixin(targets = "net.minecraft.entity.mob.GuardianEntity$GuardianTargetPredicate")
abstract class MixinGuardianTargetPredicate {
    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    private void test(@Nullable LivingEntity livingEntity, CallbackInfoReturnable<Boolean> info) {
        if (EquinePredicates.PLAYER_SEAPONY.test(livingEntity)) {
            info.setReturnValue(false);
        }
    }
}
