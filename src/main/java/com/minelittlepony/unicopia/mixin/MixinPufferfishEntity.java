package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.effect.SeaponyGraceStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PufferfishEntity;

@Mixin(PufferfishEntity.class)
abstract class MixinPufferfishEntity extends FishEntity {
    MixinPufferfishEntity() { super(null, null); }

    @Inject(method = "method_6591(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private static void unicopia_excludeSeaponysGrace(LivingEntity entity, CallbackInfoReturnable<Boolean> info) {
        if (SeaponyGraceStatusEffect.hasGrace(entity)) {
            info.setReturnValue(false);
        }
    }
}
