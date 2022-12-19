package com.minelittlepony.unicopia;

import net.minecraft.entity.Entity;

/**
 * Interface for things that can be owned by an entity.
 * <p>
 * Ownership is retained so long as the owner is still active. If the owner leaves or dies, the link is broken.
 *
 * @param <E> The type of object that owns us.
 */
public interface EntityConvertable<E extends Entity> {
    E asEntity();
}
