package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.entity.player.PlayerCamera;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;

@Mixin(Camera.class)
abstract class MixinCamera {
    @Shadow
    private float pitch;
    @Shadow
    private float yaw;

    @Inject(method = "updateRotation()V", at = @At("HEAD"))
    private void updateRotation() {
        PlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null) {
            PlayerCamera view = Pony.of(player).getCamera();

            //event.setRoll(view.calculateRoll());
            pitch = view.calculatePitch(pitch);
            yaw = view.calculateYaw(yaw);
        }
    }
}
