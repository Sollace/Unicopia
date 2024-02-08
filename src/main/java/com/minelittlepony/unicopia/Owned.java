package com.minelittlepony.unicopia;

import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.item.FriendshipBraceletItem;

import net.minecraft.entity.Entity;

/**
 * Interface for things that can be owned by an entity.
 * <p>
 * Ownership is retained so long as the owner is still active. If the owner leaves or dies, the link is broken.
 *
 * @param <E> The type of object that owns us.
 */
public interface Owned<E extends Entity> {
    /**
     * Gets the owner that holds this object.
     */
    @Nullable
    E getMaster();

    /**
     * Gets the unique entity id of the entity that holds this object.
     * <p>
     * Since {@link Owned#getMaster()} will only return if the owner is loaded, use this to perform checks
     * in the owner's absence.
     */
    Optional<UUID> getMasterId();

    default boolean isOwnerOrFriend(Entity target) {
        return isFriend(target) || isOwnerOrVehicle(target);
    }

    default boolean isFriend(Entity target) {
        return FriendshipBraceletItem.isComrade(this, target);
    }

    default boolean isOwnerOrVehicle(@Nullable Entity target) {
        if (isOwnedBy(target)) {
            return true;
        }

        Entity owner = getMaster();
        return target != null && owner != null && owner.isConnectedThroughVehicle(target);
    }

    default boolean isOwnedBy(@Nullable Object owner) {
        return owner instanceof Entity e && e.getUuid().equals(getMasterId().orElse(null));
    }

    default boolean hasCommonOwner(Owned<?> sibling) {
        return getMasterId().isPresent() && getMasterId().equals(sibling.getMasterId());
    }

    interface Mutable<E extends Entity> {
        /**
         * Updates the owner of this object.
         */
        void setMaster(@Nullable E owner);

        /**
         * Updated the owner of this object to be the same as another.
         *
         * @param sibling
         */
        default void setMaster(Owned<? extends E> sibling) {
            setMaster(sibling.getMaster());
        }
    }
}
