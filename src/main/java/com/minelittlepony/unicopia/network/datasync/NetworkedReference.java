package com.minelittlepony.unicopia.network.datasync;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtCompound;

public interface NetworkedReference<T> {
    Optional<T> getReference();

    Optional<T> updateReference(@Nullable T newValue);

    boolean fromNbt(NbtCompound comp);

    NbtCompound toNbt();

    boolean isDirty();
}
