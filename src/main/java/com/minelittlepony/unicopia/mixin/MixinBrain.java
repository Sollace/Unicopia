package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minelittlepony.unicopia.entity.Equine;
import com.minelittlepony.unicopia.entity.Living;
import com.minelittlepony.unicopia.entity.duck.RotatedView;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.server.world.ServerWorld;

@Mixin(Brain.class)
abstract class MixinBrain<E extends LivingEntity> {

    @Inject(method = "tick", at = @At("HEAD"))
    public void beforeTickAi(ServerWorld world, E entity, CallbackInfo into) {
        Equine<?> eq = Equine.of(entity).orElse(null);

        if (eq instanceof Living<?> && eq.getPhysics().isGravityNegative()) {
            ((RotatedView)world).pushRotation((int)entity.getY());
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void afterTickAi(ServerWorld world, E entity, CallbackInfo into) {
        ((RotatedView)world).popRotation();
    }
}
