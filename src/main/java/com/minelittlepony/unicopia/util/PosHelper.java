package com.minelittlepony.unicopia.util;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.util.shape.Shape;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class PosHelper {

    public static Vec3d offset(Vec3d a, Vec3i b) {
        return a.add(b.getX(), b.getY(), b.getZ());
    }

    public static BlockPos findSolidGroundAt(World world, BlockPos pos) {
        while ((pos.getY() > 0 || !World.isHeightInvalid(pos))
                && (world.isAir(pos) || world.getBlockState(pos).canPlaceAt(world, pos))) {
            pos = pos.down();
        }

        return pos;
    }

    public static void all(BlockPos origin, Consumer<BlockPos> consumer, Direction... directions) {
        for (Direction facing : directions) {
            consumer.accept(origin.offset(facing));
        }
    }

    public static boolean some(BlockPos origin, Predicate<BlockPos> consumer, Direction... directions) {
        for (Direction facing : directions) {
            if (consumer.test(origin.offset(facing))) {
                return true;
            }
        }
        return false;
    }

    public static Iterators<BlockPos> adjacentNeighbours(BlockPos origin) {
        BlockPos.Mutable pos = new BlockPos.Mutable(origin);
        Iterator<Direction> directions = Lists.newArrayList(Direction.values()).iterator();

        return Iterators.iterate(() -> {
            if (directions.hasNext()) {
                Direction next = directions.next();

                pos.set(origin.getX() + next.getOffsetX(), origin.getY() + next.getOffsetY(), origin.getZ() + next.getOffsetZ());
                return pos;
            }

            return null;
        });
    }

    public static Iterators<BlockPos> getAllInRegionMutable(BlockPos origin, Shape shape) {
        Iterator<BlockPos> iter = BlockPos.iterate(
                origin.add(new BlockPos(shape.getLowerBound())),
                origin.add(new BlockPos(shape.getUpperBound()))
            ).iterator();

        return Iterators.iterate(() -> {
            while (iter.hasNext()) {
                BlockPos pos = iter.next();

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
    public static Stream<BlockPos> inRegion(BlockPos from, BlockPos to) {
        return BlockPos.stream(from, to);
    }
}
