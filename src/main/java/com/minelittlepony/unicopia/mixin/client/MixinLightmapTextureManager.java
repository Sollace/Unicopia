package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.minelittlepony.unicopia.EquinePredicates;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

@Mixin(LightmapTextureManager.class)
abstract class MixinLightmapTextureManager implements AutoCloseable {
    @Redirect(method = "update(F)V",
            at = @At(value = "INVOKE",
                    target = "net/minecraft/client/network/ClientPlayerEntity.hasStatusEffect(Lnet/minecraft/entity/effect/StatusEffect;)Z")
    )
    private boolean redirectHasStatusEffect(ClientPlayerEntity entity, StatusEffect effect) {
        return (effect == StatusEffects.NIGHT_VISION && EquinePredicates.PLAYER_BAT.test(entity)) ||  entity.hasStatusEffect(effect);
    }
}
