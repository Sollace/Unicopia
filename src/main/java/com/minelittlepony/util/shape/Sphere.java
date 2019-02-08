package com.minelittlepony.util.shape;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/**
 * A sphere, or 2d circle of you so desire.
 *
 */
public class Sphere implements IShape {

    protected final Vec3d stretch;
    private final boolean hollow;
    private final double rad;

    private float yaw = 0;
    private float pitch = 0;

    private final double volume;

    /**
     * Creates a uniform sphere.
     *
     * @param hollow    True if this shape must be hollow.
     * @param radius    Sphere radius
     */
    public Sphere(boolean hollow, double radius) {
        this(hollow, radius, 1, 1, 1);
    }

    /**
     * Creates a sphere of arbitrary dimensions.
     * <p>
     * Can be used to create a flat circle by setting one of the stretch parameters to 0.
     * If you set two of them to 0 it will probably produce a line.
     *
     * @param hollow    True if this shape must be hollow.
     * @param radius    Sphere radius
     * @param stretchX    Warp this shape's X-axis
     * @param stretchY    Warp this shape's Y-axis
     * @param stretchZ    Warp this shape's Z-axis
     *
     */
    public Sphere(boolean hollow, double radius, float stretchX, float stretchY, float stretchZ) {
        this.hollow = hollow;
        stretch = new Vec3d(stretchX, stretchY, stretchZ);
        rad = radius;
        volume = computeSpawnableSpace();
    }

    public double getVolumeOfSpawnableSpace() {
        return volume;
    }

    private double computeSpawnableSpace() {
        if (hollow) {
            if (stretch.x == stretch.x && stretch.y == stretch.z) {
                double radius = rad * stretch.x;
                return 4 * Math.PI * radius * radius;
            }
            return computeEllipsoidArea(rad, stretch);
        }
        return computeEllipsoidVolume(rad, stretch);
    }

    public static double computeEllipsoidArea(double rad, Vec3d stretch) {
        double p = 1.6075;
        double result = Math.pow(rad * stretch.x, p) * Math.pow(rad * stretch.y, p);
        result += Math.pow(rad * stretch.x, p) * Math.pow(rad * stretch.z, p);
        result += Math.pow(rad * stretch.y, p) * Math.pow(rad * stretch.y, p);
        result /= 3;
        return 2 * Math.PI * Math.pow(result, 1/p);
    }

    public static double computeEllipsoidVolume(double rad, Vec3d stretch) {
        double result = (4/3) * Math.PI;
        result *= (rad * stretch.x);
        result *= (rad * stretch.y);
        result *= (rad * stretch.z);
        return result;
    }

    public double getXOffset() {
        return 0;
    }

    public double getYOffset() {
        return 0;
    }

    public double getZOffset() {
        return 0;
    }

    public Vec3d computePoint(Random rand) {
        double rad = this.rad;

        if (!hollow) {
            rad = MathHelper.nextDouble(rand, 0, rad);
        }

        double z = MathHelper.nextDouble(rand, -rad, rad);
        double phi = MathHelper.nextDouble(rand, 0, Math.PI * 2);
        double theta = Math.asin(z / rad);

        return new Vec3d(rad * Math.cos(theta) * Math.cos(phi) * stretch.x, rad * Math.cos(theta) * Math.sin(phi) * stretch.y, z * stretch.z).rotateYaw(yaw).rotatePitch(pitch);
    }

    public Sphere setRotation(float u, float v) {
        yaw = u;
        pitch = v;
        return this;
    }

    public boolean isPointInside(Vec3d point) {
        point = point.rotateYaw(-yaw).rotatePitch(-pitch);
        point = new Vec3d(point.x / stretch.x, point.y / stretch.y, point.z / stretch.z);

        double dist = point.length();

        return hollow ? dist == rad : dist <= rad;
    }

    @Override
    public Vec3d getLowerBound() {
        return new Vec3d(-rad * stretch.x, -rad * stretch.y, -rad * stretch.z).rotateYaw(yaw).rotatePitch(pitch);
    }

    @Override
    public Vec3d getUpperBound() {
        return new Vec3d(rad * stretch.x, rad * stretch.y, rad * stretch.z).rotateYaw(yaw).rotatePitch(pitch);
    }
}
