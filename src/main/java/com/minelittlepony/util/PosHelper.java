package com.minelittlepony.util;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Streams;
import com.minelittlepony.util.shape.IShape;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.MutableBlockPos;

public class PosHelper {

    public static void all(BlockPos origin, Consumer<BlockPos> consumer, EnumFacing... directions) {
        for (EnumFacing facing : directions) {
            consumer.accept(origin.offset(facing));
        }
    }

    public static boolean some(BlockPos origin, Function<BlockPos, Boolean> consumer, EnumFacing... directions) {
        for (EnumFacing facing : directions) {
            if (consumer.apply(origin.offset(facing))) {
                return true;
            }
        }
        return false;
    }

    public static Iterable<MutableBlockPos> getAllInRegionMutable(BlockPos origin, IShape shape) {

        Iterator<MutableBlockPos> iter = BlockPos.getAllInBoxMutable(
                origin.add(new BlockPos(shape.getLowerBound())),
                origin.add(new BlockPos(shape.getUpperBound()))
            ).iterator();

        return () -> new AbstractIterator<MutableBlockPos>() {
            @Override
            protected MutableBlockPos computeNext() {
                while (iter.hasNext()) {
                    MutableBlockPos pos = iter.next();

                    if (shape.isPointInside(new Vec3d(pos.subtract(origin)))) {
                        return pos;
                    }
                }

                endOfData();

                return null;
            }
        };
    }

    /**
     * Creates a stream of mutable block positions ranging from the beginning position to end.
     */
    public static Stream<MutableBlockPos> inRegion(BlockPos from, BlockPos to) {
        return Streams.stream(BlockPos.getAllInBoxMutable(from, to));
    }
}
