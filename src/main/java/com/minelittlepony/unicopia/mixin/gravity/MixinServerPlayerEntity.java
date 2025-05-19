package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.minelittlepony.unicopia.entity.Equine;

import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayerEntity.class)
abstract class MixinServerPlayerEntity {
    @ModifyVariable(
            method = "handleFall(DDDZ)V",
            at = @At("HEAD"),
            ordinal = 1,
            argsOnly = true)
    private double modifyFallDistance(double value) {
        if (this instanceof Equine.Container eq && eq.get().getPhysics().isGravityNegative()) {
            return -value;
        }
        return value;
    }
}
