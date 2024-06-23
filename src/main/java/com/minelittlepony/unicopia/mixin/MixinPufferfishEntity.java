package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.effect.SeaponyGraceStatusEffect;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PufferfishEntity;

@Mixin(PufferfishEntity.class)
abstract class MixinPufferfishEntity extends FishEntity {
    MixinPufferfishEntity() { super(null, null); }

    @ModifyReturnValue(method = "method_6591(Lnet/minecraft/entity/LivingEntity;)Z", at = @At("RETURN"))
    private static boolean unicopia_excludeSeaponysGrace(boolean result, LivingEntity entity) {
        return result && !SeaponyGraceStatusEffect.hasGrace(entity);
    }
}
