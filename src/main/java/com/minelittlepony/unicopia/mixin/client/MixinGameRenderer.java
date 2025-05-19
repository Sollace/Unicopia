package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.minelittlepony.unicopia.client.BatEyesApplicator;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.minelittlepony.unicopia.client.render.shader.ViewportShader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.entity.LivingEntity;
import net.minecraft.resource.SynchronousResourceReloader;

@Mixin(value = GameRenderer.class, priority = Integer.MAX_VALUE)
abstract class MixinGameRenderer implements AutoCloseable, SynchronousResourceReloader {

    @Shadow
    private @Final MinecraftClient client;

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private double modifyFov(double initial) {
        return UnicopiaClient.getCamera().calculateFieldOfView(initial);
    }

    @Inject(method = "renderWorld", at = @At("HEAD"))
    private void beforeRenderWorld(RenderTickCounter counter, CallbackInfo info) {
        BatEyesApplicator.INSTANCE.enable();
    }

    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"))
    private void tiltViewWhenHurt(MatrixStack matrices, float tickDelta, CallbackInfo info) {
        float roll = UnicopiaClient.getCamera().calculateFirstPersonRoll();
        if (roll != 0) {
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll));
        }
    }

    @Inject(method = "renderWorld", at = @At("RETURN"))
    private void afterRenderWorld(RenderTickCounter counter, CallbackInfo info) {
        BatEyesApplicator.INSTANCE.disable();
    }

    @ModifyReturnValue(method = "getNightVisionStrength", at = @At("RETURN"))
    private static float modifyNightVisionStrength(float initial, LivingEntity entity, float tickDelta) {
        return BatEyesApplicator.getWorldBrightness(initial, entity, tickDelta);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/gl/Framebuffer.beginWrite(Z)V", shift = Shift.BEFORE))
    private void onBeforeFrameEnd(RenderTickCounter tickCounter, boolean tick, CallbackInfo info) {
        ViewportShader.INSTANCE.render(tickCounter);
    }

    @Inject(method = "onResized", at = @At("HEAD"))
    private void onResized(int width, int height, CallbackInfo info) {
        ViewportShader.INSTANCE.onResized(width, height);
    }
}
