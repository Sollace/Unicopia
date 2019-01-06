package com.minelittlepony.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.google.common.collect.Streams;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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

    /**
     * Creates a stream of mutable block positions ranging from the beginning position to end.
     */
    public static Stream<MutableBlockPos> inRegion(BlockPos from, BlockPos to) {
        return Streams.stream(BlockPos.getAllInBoxMutable(from, to));
    }
}
