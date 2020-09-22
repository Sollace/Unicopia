package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.util.NbtSerialisable;

import net.minecraft.util.math.BlockPos;

public interface Physics extends NbtSerialisable {

    double calcGravity(double worldConstant);

    float getGravityModifier();

    void setBaseGravityModifier(float constant);

    boolean isFlying();

    BlockPos getHeadPosition();

    void spawnSprintingParticles();

    default boolean isGravityNegative() {
        return getGravityModifier() < 0;
    }

    default int getGravitySignum() {
        return (int)Math.signum(getGravityModifier());
    }
}
