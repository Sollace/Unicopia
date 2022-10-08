package com.minelittlepony.unicopia.util.shape;

import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

record RotatedPointGenerator (
        PointGenerator original,
        float pitch,
        float yaw
    ) implements Shape {
    @Override
    public double getVolume() {
        return ((Shape)original).getVolume();
    }

    @Override
    public Vec3d computePoint(Random rand) {
        return rotate(original.computePoint(rand));
    }

    @Override
    public Vec3d getLowerBound() {
        return rotate(((Shape)original).getLowerBound());
    }

    @Override
    public Vec3d getUpperBound() {
        return rotate(((Shape)original).getUpperBound());
    }

    @Override
    public boolean isPointInside(Vec3d point) {
        return ((Shape)original).isPointInside(point.rotateY(-yaw).rotateX(-pitch));
    }

    private Vec3d rotate(Vec3d vec) {
        return vec.rotateX(pitch).rotateY(yaw);
    }

    @Override
    public Shape rotate(float pitch, float yaw) {
        if (pitch == 0 && yaw == 0) {
            return this;
        }

        return new RotatedPointGenerator(this, this.pitch + pitch, this.yaw + yaw);
    }
}
