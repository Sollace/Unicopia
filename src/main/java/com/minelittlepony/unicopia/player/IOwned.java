package com.minelittlepony.unicopia.player;

public interface IOwned<E> {

    void setOwner(E owner);

    E getOwner();


    @SuppressWarnings("unchecked")
    static <T> IOwned<T> cast(Object o) {
        return (IOwned<T>)o;
    }
}
