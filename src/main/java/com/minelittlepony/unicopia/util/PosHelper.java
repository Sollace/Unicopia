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

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public interface PosHelper {
    Direction[] ALL = Direction.values();
    Direction[] HORIZONTAL = Arrays.stream(Direction.values()).filter(d -> d.getAxis().isHorizontal()).toArray(Direction[]::new);

    static Vec3d offset(Vec3d a, Vec3i b) {
        return a.add(b.getX(), b.getY(), b.getZ());
    }

    static BlockPos findSolidGroundAt(World world, BlockPos pos, int signum) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        while (world.isInBuildLimit(mutable) && (world.isAir(mutable) || !world.getBlockState(mutable).canPlaceAt(world, mutable))) {
            mutable.move(Direction.DOWN, signum);
        }

        return mutable.toImmutable();
    }

    static boolean isOverVoid(World world, BlockPos pos, int signum) {
        return signum > 0 && findSolidGroundAt(world, pos, signum).getY() < world.getBottomY();
    }

    static void fastAll(BlockPos origin, Consumer<BlockPos.Mutable> consumer, Direction... directions) {
        final BlockPos immutable = origin instanceof BlockPos.Mutable m ? m.toImmutable() : origin;
        final BlockPos.Mutable mutable = origin instanceof BlockPos.Mutable m ? m : origin.mutableCopy();
        for (Direction facing : directions) {
            consumer.accept(mutable.move(facing));
            mutable.set(immutable);
        }
    }

    static boolean fastAny(BlockPos origin, Predicate<BlockPos> consumer, Direction... directions) {
        final BlockPos immutable = origin instanceof BlockPos.Mutable m ? m.toImmutable() : origin;
        final BlockPos.Mutable mutable = origin instanceof BlockPos.Mutable m ? m : origin.mutableCopy();
        try {
            for (Direction facing : directions) {
                if (consumer.test(mutable.set(immutable).move(facing))) {
                    return true;
                }
            }
            return false;
        } finally {
            mutable.set(immutable);
        }
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

    public final class PositionRecord {
        private final LongOpenHashSet visitedPositions = new LongOpenHashSet();

        public BlockPos visit(BlockPos pos) {
            visitedPositions.add(pos.asLong());
            return pos;
        }

        public boolean hasVisited(BlockPos pos) {
            return visitedPositions.contains(pos.asLong());
        }

        public int size() {
            return visitedPositions.size();
        }

        public void clear() {
            visitedPositions.clear();
        }

        public void forEach(World world, Reactor reactor) {
            forEach(new BlockPos.Mutable(), pos -> reactor.react(world, world.getBlockState(pos), pos));
        }

        public void forEach(BlockPos.Mutable mutable, Consumer<BlockPos> consumer) {
            visitedPositions.forEach(l -> consumer.accept(mutable.set(l)));
        }

        public interface Reactor {
            void react(World w, BlockState state, BlockPos pos);
        }
    }
}
