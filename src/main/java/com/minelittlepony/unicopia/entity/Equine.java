package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EntityConvertable;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.projectile.ProjectileImpactListener;
import com.minelittlepony.unicopia.util.NbtSerialisable;
import com.minelittlepony.unicopia.util.Tickable;

import net.minecraft.entity.Entity;

public interface Equine<T extends Entity> extends NbtSerialisable, Tickable, ProjectileImpactListener, EntityConvertable<T> {
    Physics getPhysics();

    Race getSpecies();

    void setSpecies(Race race);

    default Race.Composite getCompositeRace() {
        return new Race.Composite(getSpecies(), null);
    }

    /**
     * Called at the beginning of an update cycle.
     */
    boolean beforeUpdate();

    @SuppressWarnings("unchecked")
    static <E extends Entity, T extends Equine<? extends E>> Optional<T> of(@Nullable E entity) {
        return entity instanceof Container ? Optional.of(((Container<T>)entity).get()) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    static <E extends Entity, T> Optional<T> of(@Nullable E entity, Predicate<Object> typeCheck) {
        return entity instanceof Container
                ? (Optional<T>)Optional.of((Object)((Container<?>)entity).get()).filter(typeCheck)
                : Optional.empty();
    }

    interface Container<T extends Equine<?>> {
        Equine<?> create();

        T get();
    }
}
