package com.minelittlepony.unicopia.entity;

import java.util.Optional;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EntityConvertable;
import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.projectile.ProjectileImpactListener;
import com.minelittlepony.unicopia.util.Tickable;
import com.minelittlepony.unicopia.util.Untyped;
import com.minelittlepony.unicopia.util.serialization.NbtSerialisable;

import net.minecraft.entity.Entity;

public interface Equine<T extends Entity> extends NbtSerialisable, Tickable, ProjectileImpactListener, EntityConvertable<T>, EquineContext {

    Transportation getTransportation();

    void setSpecies(Race race);

    /**
     * Called at the beginning of an update cycle.
     */
    boolean beforeUpdate();

    static <E extends Entity, T extends Equine<? extends E>> Optional<T> of(@Nullable E entity) {
        return entity instanceof Container c ? Optional.of(c.get()) : Optional.empty();
    }

    static <E extends Entity, T> Optional<T> of(@Nullable E entity, Predicate<Object> typeCheck) {
        return entity instanceof Equine.Container c
                ? Optional.of(c.get()).filter(typeCheck).map(Untyped::cast)
                : Optional.empty();
    }

    static <E extends Entity, T extends Equine<E>> @Nullable T cast(@Nullable E entity, Predicate<Object> typeCheck) {
        return entity instanceof Equine.Container c && typeCheck.test(c.get())
                ? Untyped.cast(c.<Equine<E>>get())
                : null;
    }

    static <T extends Equine<? extends Entity>> @Nullable T cast(@Nullable Entity entity) {
        return entity instanceof Container c ? c.<T>get() : null;
    }

    interface Container {
        Equine<?> create();

        <T extends Equine<?>> T get();
    }
}
