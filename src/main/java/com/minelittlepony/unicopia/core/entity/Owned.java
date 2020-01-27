package com.minelittlepony.unicopia.core.entity;

/**
 * Interface for things that can be owned.
 *
 * @param <E> The type of object that owns us.
 */
public interface Owned<E> {

    /**
     * Updates the owner of this object.
     */
    void setOwner(E owner);

    /**
     * Gets the owner that holds this object.
     */
    E getOwner();


    @SuppressWarnings("unchecked")
    static <T> Owned<T> cast(Object o) {
        return (Owned<T>)o;
    }
}
