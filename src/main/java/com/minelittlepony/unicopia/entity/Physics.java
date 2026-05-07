package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public interface Physics extends NbtSerialisable {

    double calcGravity(double worldConstant);

    Vec3d getMotionAngle();

    float getGravityModifier();

    float getPersistantGravityModifier();

    float getBaseGravityModifier();

    void setBaseGravityModifier(float constant);

    boolean isFlying();

    BlockPos getHeadPosition();

    default boolean isGravityNegative() {
        return getGravityModifier() < 0;
    }

    default int getGravitySignum() {
        return (int)Math.signum(getGravityModifier());
    }

    default Box getBoxAtPosition(Vec3d pos) {
        return getBoxAtPosition(pos, isGravityNegative());
    }

    Box getBoxAtPosition(Vec3d pos, boolean inverted);
}
