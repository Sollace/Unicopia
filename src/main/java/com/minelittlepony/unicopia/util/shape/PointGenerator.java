package com.minelittlepony.unicopia.util.shape;

import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.Streams;

import net.minecraft.util.math.Vec3d;
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

    default PointGenerator union(PointGenerator other) {
        final PointGenerator source = this;
        return new PointGenerator() {

            @Override
            public double getVolumeOfSpawnableSpace() {
                return source.getVolumeOfSpawnableSpace() + other.getVolumeOfSpawnableSpace();
            }

            @Override
            public Vec3d computePoint(Random rand) {
                return source.computePoint(rand);
            }

            @Override
            public Stream<Vec3d> randomPoints(int n, Random rand) {
                double total = this.getVolumeOfSpawnableSpace();

                return Streams.concat(
                        source.randomPoints((int)(n * (source.getVolumeOfSpawnableSpace() / total)), rand),
                        other.randomPoints((int)(n * (other.getVolumeOfSpawnableSpace() / total)), rand)
                );
            }
        };
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

        };
    }
}
