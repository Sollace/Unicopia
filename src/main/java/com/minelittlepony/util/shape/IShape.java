package com.minelittlepony.util.shape;

import java.util.Random;

import net.minecraft.util.math.Vec3d;

/**
 *
 *Interface for a 3d shape, used for spawning particles in a designated area (or anything else you need shapes for).
 */
public interface IShape {

    /**
     * Rotates this shape around it's center.
     *
     * @param u        Rotate yaw
     * @param v        Rotate pitch
     *
     * @return This Shape
     */
    public IShape setRotation(float u, float v);

    /**
     * Get the volume of space filled by this shape, or the surface area if hollow.
     *
     * @return double volume
     */
    public double getVolumeOfSpawnableSpace();

    /**
     * X offset from the shape's origin.
     *
     * @return X
     */
    public double getXOffset();

    /**
     * Y offset from the shape's origin.
     *
     * @return Y
     */
    public double getYOffset();

    /**
     * Z offset from the shape's origin.
     *
     * @return Z
     */
    public double getZOffset();

    /**
     * Gets the lower bounds of the region occupied by this shape.
     */
    public Vec3d getLowerBound();

    /**
     * Gets the upper bound of the region occupied by this shape.
     */
    public Vec3d getUpperBound();

    /**
     * Computes a random coordinate that falls within this shape's designated area.
     */
    public Vec3d computePoint(Random rand);

    /**
     * Checks if the given point is on the edge, or if not hollow the inside, of this shape.
     * @return
     */
    public boolean isPointInside(Vec3d point);
}
