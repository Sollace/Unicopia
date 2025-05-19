package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;

@Mixin(TargetPredicate.class)
abstract class MixinTargetPredicate {
    @Inject(method = "test", at = @At("HEAD"), cancellable = true)
    public void onTest(@Nullable LivingEntity baseEntity, LivingEntity targetEntity, CallbackInfoReturnable<Boolean> info) {
        Pony.of(targetEntity).ifPresent(pony -> {
            if (!pony.canBeSeenBy(baseEntity)) {
                info.setReturnValue(false);
            }
        });
    }
}
