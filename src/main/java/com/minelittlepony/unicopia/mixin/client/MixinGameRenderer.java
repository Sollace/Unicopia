package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.client.BatEyesApplicator;
import com.minelittlepony.unicopia.client.UnicopiaClient;
import com.minelittlepony.unicopia.client.render.shader.ViewportShader;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.resource.SynchronousResourceReloader;

@Mixin(value = GameRenderer.class, priority = Integer.MAX_VALUE)
abstract class MixinGameRenderer implements AutoCloseable, SynchronousResourceReloader {

    @Shadow
    private @Final MinecraftClient client;

    @Inject(method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D",
            at = @At("RETURN"),
            cancellable = true)
    private void onGetFov(Camera camera, float f, boolean z, CallbackInfoReturnable<Double> info) {
        UnicopiaClient.getCamera().ifPresent(c -> info.setReturnValue(c.calculateFieldOfView(info.getReturnValue())));
    }

    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At("HEAD"))
    private void beforeRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo info) {
        UnicopiaClient.getCamera().ifPresent(c -> matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(c.calculateRoll())));
        BatEyesApplicator.INSTANCE.enable();
    }

    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At("RETURN"))
    private void afterRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo info) {
        BatEyesApplicator.INSTANCE.disable();
    }

    @Inject(method = "getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F",
            at = @At("HEAD"),
            cancellable = true)
    private static void onGetNightVisionStrengthHead(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> info) {
        if (!entity.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            info.setReturnValue(UnicopiaClient.getWorldBrightness(0));
        }
    }
    @Inject(method = "getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F",
            at = @At("RETURN"),
            cancellable = true)
    private static void onGetNightVisionStrengthReturn(LivingEntity entity, float tickDelta, CallbackInfoReturnable<Float> info) {
        if (entity.hasStatusEffect(StatusEffects.NIGHT_VISION) && EquinePredicates.PLAYER_BAT.test(entity)) {
            info.setReturnValue(UnicopiaClient.getWorldBrightness(info.getReturnValueF()));
        }
    }

    @Inject(method = "render",
            at = @At(
                value = "INVOKE",
                target = "net/minecraft/client/gl/Framebuffer.beginWrite(Z)V",
                shift = Shift.BEFORE)
    )
    private void onBeforeFrameEnd(float tickDelta, long startTime, boolean tick, CallbackInfo info) {
        ViewportShader.INSTANCE.render(tickDelta);
    }

    @Inject(method = "onResized", at = @At("HEAD"))
    private void onResized(int width, int height, CallbackInfo info) {
        ViewportShader.INSTANCE.onResized(width, height);
    }
}
