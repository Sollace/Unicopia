package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(AbstractClientPlayerEntity.class)
abstract class MixinAbstractClientPlayerEntity extends PlayerEntity {
    MixinAbstractClientPlayerEntity() {
        super(null, null, 0, null);
    }

    @ModifyReturnValue(method = "getFovMultiplier(ZF)F", at = @At("RETURN"))
    public float modifyFieldOfView(float initial, boolean firstPerson, float fovEffectScale) {
        return Pony.of(this).getCamera().calculateFieldOfView(initial, firstPerson, fovEffectScale);
    }
}
