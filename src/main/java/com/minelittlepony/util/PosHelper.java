package com.minelittlepony.util;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
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

    public static boolean some(BlockPos origin, Predicate<BlockPos> consumer, EnumFacing... directions) {
        for (EnumFacing facing : directions) {
            if (consumer.test(origin.offset(facing))) {
                return true;
            }
        }
        return false;
    }

    public static Iterators<MutableBlockPos> adjacentNeighbours(BlockPos origin) {
        MutableBlockPos pos = new MutableBlockPos(origin);
        Iterator<EnumFacing> directions = Lists.newArrayList(EnumFacing.VALUES).iterator();

        return Iterators.iterate(() -> {
            if (directions.hasNext()) {
                EnumFacing next = directions.next();

                pos.setPos(origin.getX() + next.getXOffset(), origin.getY() + next.getYOffset(), origin.getZ() + next.getZOffset());
                return pos;
            }

            return null;
        });
    }

    public static Iterators<MutableBlockPos> getAllInRegionMutable(BlockPos origin, IShape shape) {
        Iterator<MutableBlockPos> iter = BlockPos.getAllInBoxMutable(
                origin.add(new BlockPos(shape.getLowerBound())),
                origin.add(new BlockPos(shape.getUpperBound()))
            ).iterator();

        return Iterators.iterate(() -> {
            while (iter.hasNext()) {
                MutableBlockPos pos = iter.next();

                if (shape.isPointInside(new Vec3d(pos.subtract(origin)))) {
                    return pos;
                }
            }

            return null;
        });
    }

    /**
     * Creates a stream of mutable block positions ranging from the beginning position to end.
     */
    public static Stream<MutableBlockPos> inRegion(BlockPos from, BlockPos to) {
        return Streams.stream(BlockPos.getAllInBoxMutable(from, to));
    }
}
