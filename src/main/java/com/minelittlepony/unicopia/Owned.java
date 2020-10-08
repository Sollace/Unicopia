package com.minelittlepony.unicopia;

/**
 * Interface for things that can be owned.
 *
 * @param <E> The type of object that owns us.
 */
public interface Owned<E> {

    /**
     * Updates the owner of this object.
     */
    void setMaster(E owner);

    /**
     * Gets the owner that holds this object.
     */
    E getMaster();
}
