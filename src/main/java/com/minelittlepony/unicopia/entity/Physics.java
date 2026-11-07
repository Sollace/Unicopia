package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public interface Physics extends NbtSerialisable {
    float DEFAULT_GRAVITY_FACTOR = 1;

    default double calcGravity(double worldConstant) {
        return worldConstant;
    }

    Vec3d getMotionAngle();

    @Override
    default void toNBT(NbtCompound compound, WrapperLookup lookup) {
    }

    @Override
    default void fromNBT(NbtCompound compound, WrapperLookup lookup) {
    }

    default float getGravityModifier() {
        return DEFAULT_GRAVITY_FACTOR;
    }

    default float getPersistantGravityModifier() {
        return DEFAULT_GRAVITY_FACTOR;
    }

    default float getBaseGravityModifier() {
        return DEFAULT_GRAVITY_FACTOR;
    }

    default void setBaseGravityModifier(float constant) {

    }

    default boolean isFlying() {
        return false;
    }

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

    interface Source {
        Physics getPhysics();
    }
}
