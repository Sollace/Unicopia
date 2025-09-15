package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.client.minelittlepony.EntityLookupAccessor;
import com.minelittlepony.unicopia.client.render.WorldRenderDelegate;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.entity.EntityLookup;

@Mixin(ClientWorld.class)
abstract class MixinClientWorld implements EntityLookupAccessor {
    @Override
    @Invoker("getEntityLookup")
    public abstract EntityLookup<Entity> callGetEntityLookup();

    @Inject(method = "getSkyColor", at = @At("HEAD"), cancellable = true)
    public void getSkyColor(Vec3d cameraPos, float tickDelta, CallbackInfoReturnable<Vec3d> info) {
        WorldRenderDelegate.INSTANCE.getSkyColor(tickDelta).ifPresent(info::setReturnValue);
    }
}
