package com.minelittlepony.unicopia.item;

public interface IMagicalItem {
    /**
     * If true this item serves as host to its own inner dimensional space.
     * Bag of Holding will explode if you try to store items of this kind inside of it.
     */
    default boolean hasInnerSpace() {
        return false;
    }
}
