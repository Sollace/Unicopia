package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(Camera.class)
abstract class MixinCamera {
    @ModifyVariable(method = "setRotation(FF)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private float modifyYaw(float yaw) {
        PlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null && MinecraftClient.getInstance().cameraEntity == player) {
            return Pony.of(player).getCamera().calculateYaw(yaw);
        }

        return yaw;
    }

    @ModifyVariable(method = "setRotation(FF)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true)
    private float modifyPitch(float pitch) {
        PlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null && MinecraftClient.getInstance().cameraEntity == player) {
            return Pony.of(player).getCamera().calculatePitch(pitch);
        }

        return pitch;
    }
}
