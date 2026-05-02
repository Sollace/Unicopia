package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;
import com.minelittlepony.unicopia.client.UnicopiaClient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

@Mixin(value = SkyRendering.class, priority = 1001)
abstract class MixinSkyRendering {
    @Inject(method = "renderCelestialBodies", at = @At(
        value = "INVOKE",
        target = "net/minecraft/client/util/math/MatrixStack.multiply(Lorg/joml/Quaternionfc;)V",
        ordinal = 0
    ))
    private void onRenderCelestialBodies(CallbackInfo info, @Local(argsOnly = true) MatrixStack matrices) {
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(UnicopiaClient.getInstance().getSkyAngleDelta(MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false))));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(UnicopiaClient.getInstance().tangentalSkyAngle.getValue()));
    }
}