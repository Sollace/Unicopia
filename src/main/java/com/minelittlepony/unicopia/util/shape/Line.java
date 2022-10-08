package com.minelittlepony.unicopia.util.shape;

import com.minelittlepony.unicopia.util.VecHelper;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

/**
 * A lonely Line. The simplest form of shape.
 */
public class Line implements Shape {

    private final double length;

    private final Vec3d gradient;

    public Line(Vec3d start, Vec3d end) {
        Vec3d lenV = end.subtract(start);
        length = lenV.length();
        this.gradient = lenV.multiply(1/length);
    }

    public Line(double length, Vec3d gradient) {
        this.length = length;
        this.gradient = gradient.normalize();
    }

    @Override
    public double getVolume() {
        return length;
    }

    @Override
    public Vec3d computePoint(Random rand) {
        return gradient.multiply(MathHelper.nextDouble(rand, 0, length));
    }

    @Override
    public boolean isPointInside(Vec3d point) {
        Vec3d divided = VecHelper.divide(point, gradient);
        return divided.x == divided.y && divided.x == divided.z;
    }

    @Override
    public Vec3d getLowerBound() {
        return Vec3d.ZERO;
    }

    @Override
    public Vec3d getUpperBound() {
        return gradient.multiply(length);
    }
}
