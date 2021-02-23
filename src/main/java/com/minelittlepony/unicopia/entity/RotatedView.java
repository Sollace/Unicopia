package com.minelittlepony.unicopia.entity;

import net.minecraft.util.math.BlockPos;

public interface RotatedView {

    void setRotationCenter(int y, int increments);

    int getRotationY();

    int getRotationIncrements();

    default void clearRotation() {
        setRotationCenter(0, 0);
    }

    default BlockPos applyRotation(BlockPos pos) {
        int newY = applyRotation(pos.getY());
        if (newY == pos.getY()) {
            return pos;
        }
        return new BlockPos(pos.getX(), applyRotation(pos.getY()), pos.getZ());
    }

    default int applyRotation(int y) {
        if (getRotationIncrements() == 0) {
            return y;
        }
        return y - ((y - getRotationY()) * 2);
    }

}
