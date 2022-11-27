package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.EquinePredicates;
import com.minelittlepony.unicopia.client.BatEyesApplicator;
import com.minelittlepony.unicopia.client.UnicopiaClient;

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

    @ModifyConstant(
            method = "updateTargetedEntity",
            constant = @Constant(doubleValue = 6),
            require = 0
            /* This injection is only here to fix reach distance in creative. If it fails, another mod is probably doing the same thing as us. */
            // TODO: Find a better way of doing this
    )
    private double onUpdateTargetedEntity(double initial) {
        return Math.max(initial, client.interactionManager.getReachDistance());
    }

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
}
