package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.player.Pony;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.Vector3f;
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

    @Inject(method = "setRotation(FF)V",
            at = @At(value = "INVOKE",
                target = "Lnet/minecraft/util/math/Quaternion;set(FFFF)V",
                shift = Shift.AFTER)
    )
    private void onSetRotation(float yaw, float pitch, CallbackInfo info) {
        PlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null && MinecraftClient.getInstance().cameraEntity == player) {
            float roll = Pony.of(player).getCamera().calculateRoll();
            ((Camera)(Object)this).getRotation().hamiltonProduct(Vector3f.POSITIVE_Z.getDegreesQuaternion(roll));
        }
    }
}
