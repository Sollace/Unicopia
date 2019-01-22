package com.minelittlepony.unicopia.util.serialisation;

import net.minecraft.nbt.NBTTagCompound;

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
}
