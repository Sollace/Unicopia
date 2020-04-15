package com.minelittlepony.unicopia.entity;

/**
 * Interface for objects that receive regular updates.
 */
@FunctionalInterface
public interface Updatable {
    /**
     * Called to update the internal logic.
     */
    void onUpdate();
}
