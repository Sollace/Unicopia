package com.minelittlepony.unicopia;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.EntityReference;
import com.minelittlepony.unicopia.entity.EntityReference.EntityValues;

import net.minecraft.entity.Entity;

/**
 * Interface for things that can be weakly owned (by an entity).
 * Ownership links for these kinds of owned instances are preserved even if the owner is not present to oversee it.
 *
 * @param <E> The type of object that owns us.
 */
public interface WeaklyOwned<E extends Entity> extends Owned<E>, WorldConvertable {
    EntityReference<E> getMasterReference();

    @Nullable
    @Override
    default E getMaster() {
        return getMasterReference().get(asWorld());
    }

    @Override
    default Optional<UUID> getMasterId() {
        return getMasterReference().getTarget().map(EntityValues::uuid);
    }

    interface Mutable<E extends Entity> extends WeaklyOwned<E>, Owned.Mutable<E> {
        @Override
        EntityReference<E> getMasterReference();

        /**
         * Updated the owner of this object to be the same as another.
         *
         * @param sibling
         */
        @Override
        @SuppressWarnings("unchecked")
        default void setMaster(Owned<? extends E> sibling) {
            if (sibling instanceof WeaklyOwned w) {
                getMasterReference().copyFrom(w.getMasterReference());
            } else {
                setMaster(sibling.getMaster());
            }
        }

        @Override
        default void setMaster(E master) {
            getMasterReference().set(master);
        }
    }
}
