package com.minelittlepony.unicopia.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
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
}
