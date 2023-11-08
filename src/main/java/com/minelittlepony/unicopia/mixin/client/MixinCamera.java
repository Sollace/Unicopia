package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.client.UnicopiaClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.CameraSubmersionType;

@Mixin(Camera.class)
abstract class MixinCamera {
    @ModifyVariable(method = "setRotation(FF)V",
            at = @At("HEAD"),
            ordinal = 0,
            argsOnly = true)
    private float modifyYaw(float yaw) {
        return UnicopiaClient.getCamera().map(c -> c.calculateYaw(yaw)).orElse(yaw);
    }

    @ModifyVariable(method = "setRotation(FF)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true)
    private float modifyPitch(float pitch) {
        return UnicopiaClient.getCamera().map(c -> c.calculatePitch(pitch)).orElse(pitch);
    }

    @Inject(method = "clipToSpace(D)D",
            at = @At("RETURN"),
            cancellable = true)
    private void redirectCameraDistance(double initial, CallbackInfoReturnable<Double> info) {
        UnicopiaClient.getCamera().flatMap(c -> c.calculateDistance(info.getReturnValueD())).ifPresent(info::setReturnValue);
    }

    @Inject(method = "getSubmersionType",
            at = @At("RETURN"),
            cancellable = true)
    public void getSubmersionType(CallbackInfoReturnable<CameraSubmersionType> info) {
        UnicopiaClient.getCamera().flatMap(c -> c.getSubmersionType(info.getReturnValue())).ifPresent(info::setReturnValue);
    }
}
