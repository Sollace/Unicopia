package com.minelittlepony.unicopia.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.Vec3d;

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

    static ListTag writeVector(Vec3d vector) {
        ListTag list = new ListTag();
        list.add(DoubleTag.of(vector.getX()));
        list.add(DoubleTag.of(vector.getY()));
        list.add(DoubleTag.of(vector.getZ()));
        return list;
    }

    static Vec3d readVector(ListTag list) {
        return new Vec3d(list.getDouble(0), list.getDouble(1), list.getDouble(2));
    }
}
