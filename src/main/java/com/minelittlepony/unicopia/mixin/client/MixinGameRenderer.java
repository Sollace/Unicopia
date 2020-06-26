package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.equine.player.Pony;
import com.minelittlepony.unicopia.world.client.render.WorldRenderDelegate;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.SynchronousResourceReloadListener;

@Mixin(GameRenderer.class)
abstract class MixinGameRenderer implements AutoCloseable, SynchronousResourceReloadListener {
    @Inject(method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D",
            at = @At("RETURN"),
            cancellable = true)
    private void onGetFov(Camera camera, float f, boolean z, CallbackInfoReturnable<Double> info) {
        info.setReturnValue(Pony.of(MinecraftClient.getInstance().player)
                .getCamera()
                .calculateFieldOfView(info.getReturnValue()));
    }

    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At("HEAD"))
    public void onRenderWorld(float tickDelta, long limitTime, MatrixStack matrices, CallbackInfo info) {
        WorldRenderDelegate.INSTANCE.applyWorldTransform(matrices, tickDelta);
    }
}
