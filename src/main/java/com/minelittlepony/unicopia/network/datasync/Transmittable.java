package com.minelittlepony.unicopia.network.datasync;

import net.minecraft.nbt.NbtCompound;

public interface Transmittable {
    void setDirty();

    void toSyncronisedNbt(NbtCompound compound);

    void fromSynchronizedNbt(NbtCompound compound);
}
