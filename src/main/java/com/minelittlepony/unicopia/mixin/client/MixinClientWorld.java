package com.minelittlepony.unicopia.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.minelittlepony.unicopia.client.minelittlepony.EntityLookupAccessor;
import com.minelittlepony.unicopia.client.render.WorldRenderDelegate;
import com.minelittlepony.unicopia.entity.Equine;
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

    @Inject(method = "tickEntity", at = @At("HEAD"), cancellable = true)
    private void beforeTickEntity(Entity entity, CallbackInfo info) {
        if (!Equine.cast(entity).getTransportation().canTick()) {
            info.cancel();
        }
    }
}
