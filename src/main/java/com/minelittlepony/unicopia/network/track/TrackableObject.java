package com.minelittlepony.unicopia.network.track;

import java.util.UUID;

import net.minecraft.nbt.NbtCompound;

public interface TrackableObject {
    UUID getUuid();

    Status getStatus();

    NbtCompound toTrackedNbt();

    void readTrackedNbt(NbtCompound compound);

    void discard(boolean immediate);

    public enum Status {
        DEFAULT,
        NEW,
        UPDATED,
        REMOVED
    }
}
