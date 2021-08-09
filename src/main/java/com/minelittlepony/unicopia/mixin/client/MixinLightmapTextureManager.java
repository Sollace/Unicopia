package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.EquinePredicates;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(value = LightmapTextureManager.class, priority = 999)
abstract class MixinLightmapTextureManager implements AutoCloseable {

    private @Shadow boolean dirty;
    private @Shadow @Final MinecraftClient client;

    private boolean batEyesApplied;

    @ModifyVariable(method = "update(F)V", at = @At(value = "HEAD"), argsOnly = true)
    private float beforeUpdate(float delta) {
        if (dirty && client.world != null) {
            PlayerEntity player = client.player;
            if (!player.hasStatusEffect(StatusEffects.NIGHT_VISION) && EquinePredicates.PLAYER_BAT.test(player)) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 1, 1, false, false));
                batEyesApplied = true;
            }
        }

        return delta;
    }

    @ModifyVariable(method = "update(F)V", at = @At(value = "RETURN"), argsOnly = true)
    private float afterUpdate(float delta) {
        if (batEyesApplied) {
            client.player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            batEyesApplied = false;
        }
        return delta;
    }
}
