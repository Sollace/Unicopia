package com.minelittlepony.unicopia.util.serialisation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public interface InbtSerialisable {
    /**
     * Called to save this to nbt to persist state on file or to transmit over the network
     *
     * @param compound  Compound tag to write to.
     */
    default void writeToNBT(NBTTagCompound compound) {

    }

    /**
     * Called to load this state from nbt
     *
     * @param compound  Compound tag to read from.
     */
    default void readFromNBT(NBTTagCompound compound) {

    }

    default NBTTagCompound toNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        writeToNBT(compound);
        return compound;
    }

    static NBTTagCompound writeBlockPos(BlockPos pos) {
        NBTTagCompound dest = new NBTTagCompound();

        dest.setInteger("X", pos.getX());
        dest.setInteger("Y", pos.getY());
        dest.setInteger("Z", pos.getZ());

        return dest;
    }

    static BlockPos readBlockPos(NBTTagCompound compound) {
        return new BlockPos(
                compound.getInteger("X"),
                compound.getInteger("Y"),
                compound.getInteger("Z")
        );
    }
}
