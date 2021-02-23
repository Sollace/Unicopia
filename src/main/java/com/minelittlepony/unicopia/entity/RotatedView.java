package com.minelittlepony.unicopia.entity;

import java.util.Stack;

import net.minecraft.util.math.BlockPos;

public interface RotatedView {

    Stack<Integer> getRotations();

    boolean hasTransform();

    default void pushRotation(int y) {
        getRotations().add(y);
    }

    default void popRotation() {
        if (!getRotations().isEmpty()) {
            getRotations().pop();
        }
    }

    default BlockPos applyRotation(BlockPos pos) {
        int newY = applyRotation(pos.getY());
        if (newY == pos.getY()) {
            return pos;
        }
        return new BlockPos(pos.getX(), newY, pos.getZ());
    }

    default int applyRotation(int y) {
        if (!hasTransform() || getRotations().isEmpty()) {
            return y;
        }
        return y - ((y - getRotations().peek()) * 2);
    }

}
