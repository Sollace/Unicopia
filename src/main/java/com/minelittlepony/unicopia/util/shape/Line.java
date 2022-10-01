package com.minelittlepony.unicopia.util.shape;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

/**
 * A lonely Line. The simplest form of shape.
 *
 */
public class Line implements Shape {

    private final double len;

    private final double dX;
    private final double dY;
    private final double dZ;

    private final double sX;
    private final double sY;
    private final double sZ;

    /**
     * Creates a line with a given length, starting point, and gradient represented
     * by another point.
     *
     * @param length    Length of this line
     * @param startX    Offset X from origin
     * @param startY    Offset Y from origin
     * @param startZ    Offset Z from origin
     * @param deltaX    Change in X
     * @param deltaY    Change in Y
     * @param deltaZ    Change in Z
     */
    public Line(double length, double startX, double startY, double startZ, double deltaX, double deltaY, double deltaZ) {
        len = length;
        dX = deltaX;
        dY = deltaY;
        dZ = deltaZ;
        sX = startX;
        sY = startY;
        sZ = startZ;
    }

    public Line(Vec3d start, Vec3d end) {
        Vec3d lenV = end.subtract(start);

        len = lenV.length();

        sX = start.x;
        sY = start.y;
        sZ = start.z;

        dX = lenV.x / len;
        dY = lenV.y / len;
        dZ = lenV.z / len;
    }

    @Override
    public double getVolumeOfSpawnableSpace() {
        return len;
    }

    @Override
    public Vec3d computePoint(Random rand) {
        double distance = MathHelper.nextDouble(rand, 0, len);
        return new Vec3d(dX, dY, dZ).multiply(distance).add(sX, sY, sZ);
    }

    @Override
    public boolean isPointInside(Vec3d point) {
        return point.x/dX == point.y/dY && point.x/dX == point.z/dZ;
    }

    @Override
    public Vec3d getLowerBound() {
        return new Vec3d(sX, sY, sZ);
    }

    @Override
    public Vec3d getUpperBound() {
        return new Vec3d(sX + dX, sY + dY, sZ + dZ).multiply(len);
    }
}
