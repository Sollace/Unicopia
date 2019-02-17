package com.minelittlepony.unicopia.player;

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
