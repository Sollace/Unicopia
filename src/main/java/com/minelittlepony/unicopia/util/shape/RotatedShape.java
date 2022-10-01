package com.minelittlepony.unicopia.util.shape;

import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

final class RotatedShape implements Shape {
    private final float pitch;
    private final float yaw;

    private final Shape original;

    RotatedShape(Shape original, float pitch, float yaw) {
        this.original = original;
        this.pitch = pitch;
        this.yaw = yaw;
    }

    @Override
    public double getVolumeOfSpawnableSpace() {
        return original.getVolumeOfSpawnableSpace();
    }

    @Override
    public Vec3d computePoint(Random rand) {
        return original.computePoint(rand).rotateX(pitch).rotateY(yaw);
    }

    @Override
    public Vec3d getLowerBound() {
        return original.getLowerBound().rotateX(pitch).rotateY(yaw);
    }

    @Override
    public Vec3d getUpperBound() {
        return original.getUpperBound().rotateX(pitch).rotateY(yaw);
    }

    @Override
    public boolean isPointInside(Vec3d point) {
        return original.isPointInside(point.rotateY(-yaw).rotateX(-pitch));
    }

    @Override
    public Shape rotate(float pitch, float yaw) {
        if (pitch == 0 && yaw == 0) {
            return this;
        }

        return new RotatedShape(this, this.pitch + pitch, this.yaw + yaw);
    }
}
