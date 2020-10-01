package com.minelittlepony.unicopia.entity.player;

/**
 * Interface for controlling flight.
 */
public interface Motion {
    /**
     * True is we're currently flying.
     */
    boolean isFlying();

    PlayerDimensions getDimensions();
}
