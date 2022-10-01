package com.minelittlepony.unicopia.util.shape;

import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

/**
 *
 *Interface for a 3d shape, used for spawning particles in a designated area (or anything else you need shapes for).
 */
public interface PointGenerator {

    /**
     * Get the volume of space filled by this shape, or the surface area if hollow.
     *
     * @return double volume
     */
    double getVolumeOfSpawnableSpace();

    /**
     * Computes a random coordinate that falls within this shape's designated area.
     */
    Vec3d computePoint(Random rand);

    /**
     * Returns a sequence of random points dealed out to uniformly fill this shape's area.
     */
    default Stream<Vec3d> randomPoints(Random rand) {
        return randomPoints((int)getVolumeOfSpawnableSpace(), rand);
    }

    /**
     * Returns a sequence of N random points.
     */
    default Stream<Vec3d> randomPoints(int n, Random rand) {
        AtomicInteger atom = new AtomicInteger(n);
        return StreamSupport.stream(new AbstractSpliterator<Vec3d>(n, Spliterator.SIZED) {
            @Override
            public boolean tryAdvance(Consumer<? super Vec3d> consumer) {

                if (atom.decrementAndGet() >= 0) {
                    consumer.accept(computePoint(rand));
                    return true;
                }

                return false;
            }
        }, false);
    }

    /**
     * Returns a stream of block positions.
     */
    Stream<BlockPos> getBlockPositions();

    /**
     * Returns a new point generator where all of its points are offset by the specified amount.
     */
    default PointGenerator offset(Vec3i offset) {
        return offset(Vec3d.of(offset));
    }

    /**
     * Returns a new point generator where all of its points are offset by the specified amount.
     */
    default PointGenerator offset(Vec3d offset) {
        final PointGenerator source = this;
        return new PointGenerator() {

            @Override
            public double getVolumeOfSpawnableSpace() {
                return source.getVolumeOfSpawnableSpace();
            }

            @Override
            public Vec3d computePoint(Random rand) {
                return source.computePoint(rand).add(offset);
            }

            @Override
            public Stream<BlockPos> getBlockPositions() {
                return source.getBlockPositions().map(pos -> pos.add(offset.x, offset.y, offset.z));
            }
        };
    }
}
