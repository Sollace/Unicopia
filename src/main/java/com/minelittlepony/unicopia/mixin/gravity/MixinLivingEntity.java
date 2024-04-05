package com.minelittlepony.unicopia.mixin.gravity;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import com.minelittlepony.unicopia.entity.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

@Mixin(LivingEntity.class)
abstract class MixinLivingEntity extends Entity implements Equine.Container<Living<?>> {

    private MixinLivingEntity() { super(null, null); }

    @ModifyConstant(method = "travel(Lnet/minecraft/util/math/Vec3d;)V", constant = {
            @Constant(doubleValue = 0.08D),
            @Constant(doubleValue = 0.01D)
    })
    private double modifyGravity(double initial) {
        return Math.abs(get().getPhysics().calcGravity(initial));
    }

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

    @Override
    public BlockPos getBlockPos() {
        if (get().getPhysics().isGravityNegative()) {
            return get().getPhysics().getHeadPosition();
        }
        return super.getBlockPos();
    }
}
