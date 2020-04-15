package com.minelittlepony.unicopia.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;

public interface NbtSerialisable {
    /**
     * Called to save this to nbt to persist state on file or to transmit over the network
     *
     * @param compound  Compound tag to write to.
     */
    default void toNBT(CompoundTag compound) {

    }

    /**
     * Called to load this state from nbt
     *
     * @param compound  Compound tag to read from.
     */
    default void fromNBT(CompoundTag compound) {

    }

    default CompoundTag toNBT() {
        CompoundTag compound = new CompoundTag();
        toNBT(compound);
        return compound;
    }

    static CompoundTag writeBlockPos(BlockPos pos) {
        CompoundTag dest = new CompoundTag();

        dest.putInt("X", pos.getX());
        dest.putInt("Y", pos.getY());
        dest.putInt("Z", pos.getZ());

        return dest;
    }

    static BlockPos readBlockPos(CompoundTag compound) {
        return new BlockPos(
                compound.getInt("X"),
                compound.getInt("Y"),
                compound.getInt("Z")
        );
    }
}
