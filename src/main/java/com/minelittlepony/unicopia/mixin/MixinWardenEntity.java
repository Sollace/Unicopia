package com.minelittlepony.unicopia.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.entity.Living;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.WardenEntity;

@Mixin(WardenEntity.class)
abstract class MixinWardenEntity {
    @Inject(method = "isValidTarget", at = @At("HEAD"), cancellable = true)
    public void onIsValidTarget(@Nullable Entity entity, CallbackInfoReturnable<Boolean> info) {
        if (Living.getOrEmpty(entity).filter(l -> l.getCompositeRace().includes(Race.KIRIN)).isPresent()) {
            info.setReturnValue(false);
        }
    }
}
