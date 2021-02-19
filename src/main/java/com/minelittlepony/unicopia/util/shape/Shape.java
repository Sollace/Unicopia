package com.minelittlepony.unicopia.util.shape;

import net.minecraft.util.math.Vec3d;

/**
 *
 *Interface for a 3d shape, used for spawning particles in a designated area (or anything else you need shapes for).
 */
public interface Shape extends PointGenerator {

    /**
     * Rotates this shape around it's center.
     *
     * @param u        Rotate yaw
     * @param v        Rotate pitch
     *
     * @return This Shape
     */
    Shape setRotation(float u, float v);

    /**
     * X offset from the shape's origin.
     *
     * @return X
     */
    double getXOffset();

    /**
     * Y offset from the shape's origin.
     *
     * @return Y
     */
    double getYOffset();

    /**
     * Z offset from the shape's origin.
     *
     * @return Z
     */
    double getZOffset();

    /**
     * Gets the lower bounds of the region occupied by this shape.
     */
    Vec3d getLowerBound();

    /**
     * Gets the upper bound of the region occupied by this shape.
     */
    Vec3d getUpperBound();

    /**
     * Checks if the given point is on the edge, or if not hollow the inside, of this shape.
     * @return
     */
    boolean isPointInside(Vec3d point);
}
