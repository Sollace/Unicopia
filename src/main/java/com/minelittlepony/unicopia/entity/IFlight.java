package com.minelittlepony.unicopia.entity;

/**
 * Interface for controlling flight.
 */
public interface IFlight {
    /**
     * True is we're currently flying.
     */
    boolean isFlying();

    float getFlightExperience();

    float getFlightDuration();

    boolean isExperienceCritical();

}
