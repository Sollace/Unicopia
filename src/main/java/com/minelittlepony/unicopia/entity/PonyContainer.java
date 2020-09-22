package com.minelittlepony.unicopia.entity;

import java.util.Optional;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public interface PonyContainer<T extends Equine<?>> {

    Equine<?> create();

    T get();

    @SuppressWarnings("unchecked")
    @Nullable
    default <E extends LivingEntity> Caster<E> getCaster() {
        T ientity = get();

        if (ientity instanceof Caster) {
            return (Caster<E>)ientity;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    static <E extends Entity, T extends Equine<?>> Optional<PonyContainer<T>> of(Entity entity) {
        if (entity instanceof PonyContainer) {
            return Optional.of(((PonyContainer<T>)entity));
        }
        return Optional.empty();
    }
}
