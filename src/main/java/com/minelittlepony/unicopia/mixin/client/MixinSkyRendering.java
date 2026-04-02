package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.client.UnicopiaClient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.SkyRendering;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

@Mixin(value = SkyRendering.class, priority = 1001)
abstract class MixinSkyRendering {
    @Inject(method = "renderCelestialBodies", at = @At("HEAD"))
    private void beforeRenderCelestialBodies(MatrixStack matrices, Tessellator tesselator, float rot, int phase, float alpha, float starBrightness, Fog fog, CallbackInfo info) {
        matrices.push();
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(UnicopiaClient.getInstance().getSkyAngleDelta(MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(false))));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(UnicopiaClient.getInstance().tangentalSkyAngle.getValue()));
    }

    @Inject(method = "renderCelestialBodies", at = @At("RETURN"))
    private void afterRenderCelestialBodies(MatrixStack matrices, Tessellator tesselator, float rot, int phase, float alpha, float starBrightness, Fog fog, CallbackInfo info) {
        matrices.pop();
    }
}