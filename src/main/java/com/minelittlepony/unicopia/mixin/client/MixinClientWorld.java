package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.client.render.WorldRenderDelegate;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

@Mixin(ClientWorld.class)
abstract class MixinClientWorld {

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    public void getSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> info) {
        WorldRenderDelegate.INSTANCE.getSkyColor(tickDelta).ifPresent(info::setReturnValue);
    }
}
