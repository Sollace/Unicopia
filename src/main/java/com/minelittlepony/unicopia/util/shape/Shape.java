package com.minelittlepony.unicopia.util.shape;

import java.util.stream.Stream;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

/**
 *
 *Interface for a 3d shape, used for spawning particles in a designated area (or anything else you need shapes for).
 */
public interface Shape extends PointGenerator {
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
     */
    boolean isPointInside(Vec3d point);

    /**
     * Returns a stream of all block positions that fit inside this shape.
     */
    @Override
    default Stream<BlockPos> getBlockPositions() {
        return BlockPos.stream(
            new BlockPos(getLowerBound()),
            new BlockPos(getUpperBound())
        ).filter(pos -> isPointInside(Vec3d.ofCenter(pos)));
    }

    /**
     * Returns a new shape with after applying additional rotation.
     */
    default Shape rotate(float pitch, float yaw) {
        if (pitch == 0 && yaw == 0) {
            return this;
        }
        return new RotatedShape(this, pitch, yaw);
    }
}
