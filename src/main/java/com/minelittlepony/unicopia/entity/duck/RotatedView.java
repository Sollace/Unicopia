package com.minelittlepony.unicopia.entity.duck;

import java.util.Stack;

import net.minecraft.util.math.BlockPos;

public interface RotatedView {

    Stack<Integer> getRotations();

    boolean hasTransform();

    void setMirrorEntityStatuses(boolean enable);

    default void pushRotation(int y) {
        getRotations().add(y);
    }

    default void popRotation() {
        Stack<Integer> rotations = getRotations();
        synchronized (rotations) {
            if (!rotations.isEmpty()) {
                rotations.pop();
            }
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
        Stack<Integer> rotations = getRotations();
        synchronized (rotations) {
            if (!hasTransform() || rotations.isEmpty()) {
                return y;
            }

            return (rotations.peek() * 2) - y;
        }
    }

}
