package com.minelittlepony.unicopia.world.block.gas;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.EmptyBlockView;

public interface Gas {

    GasState getGasState(BlockState blockState);

    default boolean applyLanding(Entity entity, float fallDistance) {
        if (entity.isSneaking()) {
            return true;
        }

        entity.handleFallDamage(fallDistance, 0);
        return false;
    }

    default boolean isFaceCoverd(BlockState state, BlockState beside, Direction face) {
        return beside.getBlock() instanceof Gas
                && ((Gas)beside.getBlock()).getGasState(beside) == getGasState(state)
                && VoxelShapes.isSideCovered(
                    state.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN),
                    beside.getCollisionShape(EmptyBlockView.INSTANCE, BlockPos.ORIGIN), face);
    }

    default boolean applyRebound(Entity entity) {
        double y = entity.getVelocity().y;

        if (entity.isSneaking() || y >= 0 || Math.abs(y) < 0.25) {
            return false;
        }

        entity.setVelocity(entity.getVelocity().multiply(1, -1.2, 1));

        return true;
    }

    default boolean applyBouncyness(BlockState state, Entity entity) {
        if (!getGasState(state).canTouch(entity)) {
            return false;
        }

        Vec3d vel = entity.getVelocity();
        double y = vel.y;

        if (entity.isSneaking() || Math.abs(y) < 0.25) {
            y = 0;
        } else {
            y += 0.0155 * Math.max(1, entity.fallDistance);
        }
        entity.setVelocity(vel.x, y, vel.z);

        return true;
    }

    default boolean isSupporting(BlockState state) {
        return getGasState(state).isDense();
    }
}
