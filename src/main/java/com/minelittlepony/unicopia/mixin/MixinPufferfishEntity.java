package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PufferfishEntity;

@Mixin(PufferfishEntity.class)
abstract class MixinPufferfishEntity extends FishEntity {
    MixinPufferfishEntity() { super(null, null); }

    @Inject(method = "method_6591(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private static void onShouldTarget(LivingEntity entity, CallbackInfoReturnable<Boolean> info) {
        Pony.of(entity).filter(pony -> pony.getCompositeRace().includes(Race.SEAPONY)).ifPresent(pony -> {
            info.setReturnValue(false);
        });
    }
}
