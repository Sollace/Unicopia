package com.minelittlepony.util;

import java.util.function.Consumer;
import java.util.function.Function;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

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
}
