package com.minelittlepony.unicopia.entity;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;

public interface PonyContainer<T extends Equine<?>> {

    Equine<?> create();

    T get();

    @SuppressWarnings("unchecked")
    static <E extends Entity, T extends Equine<?>> Optional<PonyContainer<T>> of(@Nullable Entity entity) {
        if (entity instanceof PonyContainer) {
            return Optional.of(((PonyContainer<T>)entity));
        }
        return Optional.empty();
    }
}
