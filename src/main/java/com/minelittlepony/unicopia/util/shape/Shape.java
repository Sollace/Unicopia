package com.minelittlepony.unicopia.util.shape;

import java.util.stream.Stream;

import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

/**
 *
 *Interface for a 3d shape, used for spawning particles in a designated area (or anything else you need shapes for).
 */
public interface Shape extends PointGenerator {
    /**
     * Get the volume of space filled by this shape, or the surface area if hollow.
     *
     * @return double volume
     */
    double getVolume();

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
    default Stream<BlockPos> getBlockPositions() {
        return BlockPos.stream(
            new BlockPos(getLowerBound()),
            new BlockPos(getUpperBound())
        ).filter(pos -> isPointInside(Vec3d.ofCenter(pos)));
    }

    /**
     * Returns a sequence of random points dealed out to uniformly fill this shape's area.
     */
    default Stream<Vec3d> randomPoints(Random rand) {
        return randomPoints((int)getVolume(), rand);
    }

    /**
     * Returns a new shape with after applying additional rotation.
     */
    @Override
    default Shape rotate(float pitch, float yaw) {
        return pitch == 0 && yaw == 0 ? this : new RotatedPointGenerator(this, pitch, yaw);
    }

    @Override
    default Shape translate(Vec3i offset) {
        return offset.equals(Vec3i.ZERO) ? this : translate(Vec3d.of(offset));
    }

    @Override
    default Shape translate(Vec3d offset) {
        return offset.equals(Vec3d.ZERO) ? this : new TranslatedPointGenerator(this, offset);
    }
}
