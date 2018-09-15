package com.minelittlepony.unicopia.player;

public interface IOwned<E> {

    default void setOwner(E owner) {

    }

    E getOwner();

}
