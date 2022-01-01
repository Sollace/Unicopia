package com.minelittlepony.unicopia;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.EntityReference;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 * Interface for things that can be weakly owned (by an entity).
 * Ownership links for these kinds of owned instances are preserved even if the owner is not present to oversee it.
 *
 * @param <E> The type of object that owns us.
 */
public interface WeaklyOwned<E extends Entity> extends Owned<E> {

    World getWorld();

    EntityReference<E> getMasterReference();

    /**
     * Updated the owner of this object to be the same as another.
     *
     * @param sibling
     */
    @SuppressWarnings("unchecked")
    default void setMaster(WeaklyOwned<? extends E> sibling) {
        if (sibling instanceof WeaklyOwned) {
            getMasterReference().copyFrom(((WeaklyOwned<E>)sibling).getMasterReference());
        } else {
            setMaster(sibling.getMaster());
        }
    }

    @Nullable
    @Override
    default E getMaster() {
        return getMasterReference().get(getWorld());
    }

    @Override
    default void setMaster(E master) {
        getMasterReference().set(master);
    }

    @Override
    default Optional<UUID> getMasterId() {
        return getMasterReference().getId();
    }
}
