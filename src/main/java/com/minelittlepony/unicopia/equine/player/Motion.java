package com.minelittlepony.unicopia.equine.player;

/**
 * Interface for controlling flight.
 */
public interface Motion {
    /**
     * True is we're currently flying.
     */
    boolean isFlying();

    float getFlightExperience();

    float getFlightDuration();

    boolean isExperienceCritical();

    PlayerDimensions getDimensions();
}
