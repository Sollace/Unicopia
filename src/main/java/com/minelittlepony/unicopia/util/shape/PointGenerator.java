package com.minelittlepony.unicopia.util.shape;

import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
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
     * Computes a random coordinate that falls within this shape's designated area.
     */
    Vec3d computePoint(Random rand);

    /**
     * Returns a sequence of N random points.
     */
    default Stream<Vec3d> randomPoints(int n, Random rand) {
        return StreamSupport.stream(new AbstractSpliterator<Vec3d>(n, Spliterator.SIZED) {
            private int index = n;

            @Override
            public boolean tryAdvance(Consumer<? super Vec3d> consumer) {
                if (--index >= 0) {
                    consumer.accept(computePoint(rand));
                    return true;
                }

                return false;
            }
        }, false);
    }

    /**
     * Returns a new shape with after applying additional rotation.
     */
    default PointGenerator rotate(float pitch, float yaw) {
        return pitch == 0 && yaw == 0 ? this : new RotatedPointGenerator(this, pitch, yaw);
    }

    default PointGenerator translate(Vec3i offset) {
        return offset.equals(Vec3i.ZERO) ? this : translate(Vec3d.of(offset));
    }

    default PointGenerator translate(Vec3d offset) {
        return offset.equals(Vec3d.ZERO) ? this : new TranslatedPointGenerator(this, offset);
    }
}
