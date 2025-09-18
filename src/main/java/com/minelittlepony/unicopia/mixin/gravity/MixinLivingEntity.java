package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import com.minelittlepony.unicopia.entity.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

@Mixin(LivingEntity.class)
abstract class MixinLivingEntity extends Entity implements Equine.Container<Living<?>> {

    private MixinLivingEntity() { super(null, null); }

    @ModifyArg(method = "fall",
            at = @At(value = "INVOKE",
                target = "net/minecraft/server/world/ServerWorld.spawnParticles(Lnet/minecraft/particle/ParticleEffect;DDDIDDDD)I"),
            index = 2)
    private double modifyParticleY(double y) {
        if (get().getPhysics().isGravityNegative()) {
            return y + getHeight();
        }
        return y;
    }
}
