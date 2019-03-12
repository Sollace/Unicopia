package com.minelittlepony.unicopia.player;

/**
 * Interface for things that can be owned.
 *
 * @param <E> The type of object that owns us.
 */
public interface IOwned<E> {

    /**
     * Updates the owner of this object.
     */
    void setOwner(E owner);

    /**
     * Gets the owner that holds this object.
     */
    E getOwner();


    @SuppressWarnings("unchecked")
    static <T> IOwned<T> cast(Object o) {
        return (IOwned<T>)o;
    }
}
