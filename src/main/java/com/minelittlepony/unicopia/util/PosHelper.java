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

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public interface PosHelper {

    Direction[] HORIZONTAL = Arrays.stream(Direction.values()).filter(d -> d.getAxis().isHorizontal()).toArray(Direction[]::new);

    static Vec3d offset(Vec3d a, Vec3i b) {
        return a.add(b.getX(), b.getY(), b.getZ());
    }

    static BlockPos findSolidGroundAt(World world, BlockPos pos, int signum) {
        while (world.isInBuildLimit(pos) && (world.isAir(pos) || !world.getBlockState(pos).canPlaceAt(world, pos))) {
            pos = pos.offset(Axis.Y, -signum);
        }

        return pos;
    }

    static void all(BlockPos origin, Consumer<BlockPos> consumer, Direction... directions) {
        BlockPos.Mutable mutable = origin.mutableCopy();
        for (Direction facing : directions) {
            mutable.set(origin);
            consumer.accept(mutable.move(facing));
        }
    }

    static boolean any(BlockPos origin, Predicate<BlockPos> consumer, Direction... directions) {
        BlockPos.Mutable mutable = origin.mutableCopy();
        for (Direction facing : directions) {
            mutable.set(origin);
            if (consumer.test(mutable.move(facing))) {
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

    static BlockPos traverseChain(BlockView world, BlockPos startingPos, Direction chainDirection, Predicate<BlockState> isInChain) {
        BlockPos.Mutable mutablePos = new BlockPos.Mutable();
        mutablePos.set(startingPos);
        do {
            mutablePos.move(chainDirection);
        } while (isInChain.test(world.getBlockState(mutablePos)) && !world.isOutOfHeightLimit(mutablePos));

        mutablePos.move(chainDirection.getOpposite());
        return mutablePos.toImmutable();
    }
}
