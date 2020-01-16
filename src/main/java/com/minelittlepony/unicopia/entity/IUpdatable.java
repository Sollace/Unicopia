package com.minelittlepony.unicopia.entity;

/**
 * Interface for objects that receive regular updates.
 */
@FunctionalInterface
public interface IUpdatable {
    /**
     * Called to update the internal logic.
     */
    void onUpdate();
}
