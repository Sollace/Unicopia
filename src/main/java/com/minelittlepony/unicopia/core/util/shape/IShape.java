package com.minelittlepony.unicopia.core.util.shape;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.minelittlepony.unicopia.core.util.Iterators;

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
    IShape setRotation(float u, float v);

    /**
     * Get the volume of space filled by this shape, or the surface area if hollow.
     *
     * @return double volume
     */
    double getVolumeOfSpawnableSpace();

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
     * Computes a random coordinate that falls within this shape's designated area.
     */
    Vec3d computePoint(Random rand);

    /**
     * Checks if the given point is on the edge, or if not hollow the inside, of this shape.
     * @return
     */
    boolean isPointInside(Vec3d point);

    /**
     * Returns a sequence of N random points.
     */
    default Iterators<Vec3d> randomPoints(int n, Random rand) {
        AtomicInteger atom = new AtomicInteger(n);
        return Iterators.iterate(() -> {
            if (atom.get() <= 0) {
                return null;
            }

            atom.decrementAndGet();

            return computePoint(rand);
        });
    }
}
