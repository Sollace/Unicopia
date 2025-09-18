package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.effect.SeaponyGraceStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.PufferfishEntity;

@Mixin(PufferfishEntity.class)
abstract class MixinPufferfishEntity {
    @ModifyReturnValue(method = "method_6591(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/server/world/ServerWorld;)Z", at = @At("RETURN"))
    private static boolean unicopia_excludeSeaponysGrace(boolean matched, LivingEntity entity) {
        return matched && !SeaponyGraceStatusEffect.hasGrace(entity);
    }
}
