package com.minelittlepony.unicopia.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Vec3d;

public interface NbtSerialisable {
    /**
     * Called to save this to nbt to persist state on file or to transmit over the network
     *
     * @param compound  Compound tag to write to.
     */
    default void toNBT(NbtCompound compound) {

    }

    /**
     * Called to load this state from nbt
     *
     * @param compound  Compound tag to read from.
     */
    default void fromNBT(NbtCompound compound) {

    }

    default NbtCompound toNBT() {
        NbtCompound compound = new NbtCompound();
        toNBT(compound);
        return compound;
    }

    static NbtList writeVector(Vec3d vector) {
        NbtList list = new NbtList();
        list.add(NbtDouble.of(vector.getX()));
        list.add(NbtDouble.of(vector.getY()));
        list.add(NbtDouble.of(vector.getZ()));
        return list;
    }

    static Vec3d readVector(NbtList list) {
        return new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
    }
}
