package com.minelittlepony.unicopia.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators.AbstractSpliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.util.shape.Shape;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public interface PosHelper {

    Direction[] HORIZONTAL = Arrays.stream(Direction.values()).filter(d -> d.getAxis().isHorizontal()).toArray(Direction[]::new);

    static Vec3d offset(Vec3d a, Vec3i b) {
        return a.add(b.getX(), b.getY(), b.getZ());
    }

    static BlockPos findSolidGroundAt(World world, BlockPos pos) {
        while ((pos.getY() > 0 || !World.isInBuildLimit(pos))
                && (world.isAir(pos) || !world.getBlockState(pos).canPlaceAt(world, pos))) {
            pos = pos.down();
        }

        return pos;
    }

    static void all(BlockPos origin, Consumer<BlockPos> consumer, Direction... directions) {
        for (Direction facing : directions) {
            consumer.accept(origin.offset(facing));
        }
    }

    static boolean any(BlockPos origin, Predicate<BlockPos> consumer, Direction... directions) {
        for (Direction facing : directions) {
            if (consumer.test(origin.offset(facing))) {
                return true;
            }
        }
        return false;
    }

    static Stream<BlockPos> adjacentNeighbours(BlockPos origin) {
        return StreamSupport.stream(new AbstractSpliterator<BlockPos>(Direction.values().length, Spliterator.SIZED) {
            private final BlockPos.Mutable pos = new BlockPos.Mutable();
            private final Iterator<Direction> iter = Lists.newArrayList(Direction.values()).iterator();

            @Override
            public boolean tryAdvance(Consumer<? super BlockPos> consumer) {
                if (iter.hasNext()) {
                    Direction next = iter.next();
                    consumer.accept(pos.set(origin.getX() + next.getOffsetX(), origin.getY() + next.getOffsetY(), origin.getZ() + next.getOffsetZ()));
                    return true;
                }
                return false;
            }
        }, false);
    }

    static Stream<BlockPos> getAllInRegionMutable(BlockPos origin, Shape shape) {
        return BlockPos.stream(
            origin.add(new BlockPos(shape.getLowerBound())),
            origin.add(new BlockPos(shape.getUpperBound()))
        ).filter(pos -> shape.isPointInside(Vec3d.of(pos.subtract(origin))));
    }
}
