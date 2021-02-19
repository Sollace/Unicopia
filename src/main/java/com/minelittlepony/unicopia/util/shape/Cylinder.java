package com.minelittlepony.unicopia.util.shape;

import java.util.Random;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Cylinder implements Shape {

    private final boolean hollow;

    private final double stretchX;
    private final double stretchZ;

    private final double height;
    private final double rad;

    private float yaw = 0;
    private float pitch = 0;

    private final double volume;

    /**
     * Creates a uniform cylinder.
     *
     * @param hollow    True if this shape must be hollow.
     * @param height    Cylinder height
     * @param radius    Cylinder radius
     *
     */
    public Cylinder(boolean hollow, double height, double radius) {
        this(hollow, height, radius, 1, 1);
    }

    /**
     * Creates a cylinder of arbitrary dimensions.
     * <p>
     * Can be used to create a flat circle by setting the height to 0.
     *
     * @param hollow    True if this shape must be hollow.
     * @param height    Cylinder height
     * @param radius    Cylinder radius
     * @param stretchX  Warp this shape's X-axis
     * @param stretchZ  Warp this shape's Z-axis
     */
    public Cylinder(boolean hollow, double height, double radius, double stretchX, double stretchZ) {
        this.hollow = hollow;
        this.height = height;
        rad = radius;
        this.stretchX = stretchX;
        this.stretchZ = stretchZ;
        volume = computeSpawnableSpace();
    }

    @Override
    public double getVolumeOfSpawnableSpace() {
        return volume;
    }

    private double computeSpawnableSpace() {
        if (hollow) {
            if (stretchX != stretchZ) {
                double result = 3 * (stretchX + stretchZ);
                result -= Math.sqrt((10 * stretchX * stretchZ) + 3 * ((stretchX * stretchX) + (stretchZ * stretchZ)));
                return Math.PI * result;
            }
            return 2 * Math.PI * rad * stretchX * height;
        }
        return Math.PI * (stretchX * rad * stretchZ * rad) * height;
    }

    @Override
    public double getXOffset() {
        return 0;
    }

    @Override
    public double getYOffset() {
        return 0;
    }

    @Override
    public double getZOffset() {
        return 0;
    }

    @Override
    public Vec3d computePoint(Random rand) {
        double y = MathHelper.nextDouble(rand, 0, height);
        double pheta = MathHelper.nextDouble(rand, 0, Math.PI * 2);
        double rho = hollow && Math.abs(y) != height/2 ? rad : MathHelper.nextDouble(rand, 0, rad);

        return new Vec3d(rho * Math.cos(pheta) * stretchX, y, rho * Math.sin(pheta) * stretchZ)
                .rotateY(yaw)
                .rotateX(pitch);
    }

    @Override
    public Cylinder setRotation(float u, float v) {
        yaw = u;
        pitch = v;
        return this;
    }

    @Override
    public boolean isPointInside(Vec3d point) {
        point = point.rotateY(-yaw).rotateX(-pitch);
        point = new Vec3d(point.x / stretchX, point.y, point.z / stretchZ);
        double y = Math.abs(point.y);
        if (y < height/2) {
            double r = Math.sqrt((point.x * point.x) + (point.z * point.z));
            return hollow ? r == rad : r <= rad;
        }
        return y == height/2;
    }

    @Override
    public Vec3d getLowerBound() {
        return new Vec3d(-rad * stretchX, 0, -rad * stretchZ)
                .rotateY(yaw)
                .rotateX(pitch);
    }

    @Override
    public Vec3d getUpperBound() {
        return new Vec3d(-rad * stretchX, height, -rad * stretchZ)
                .rotateY(yaw)
                .rotateX(pitch);
    }
}