package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.duck.RotatedView;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
abstract class MixinServerWorld implements RotatedView {

    @Inject(method = "tickEntity", at = @At("HEAD"))
    private void beforeTickEntity(Entity entity, CallbackInfo info) {
        if (entity instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
           // pushRotation((int)(entity.getY() + entity.getHeight() * 0.5F));
        }
    }

    @Inject(method = "tickEntity", at = @At("RETURN"))
    private void afterTickEntity(Entity entity, CallbackInfo info) {
        if (entity instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
           // popRotation();
        }
    }
}
