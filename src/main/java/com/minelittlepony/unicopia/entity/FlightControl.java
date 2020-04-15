package com.minelittlepony.unicopia.entity;

/**
 * Interface for controlling flight.
 */
public interface FlightControl {
    /**
     * True is we're currently flying.
     */
    boolean isFlying();

    float getFlightExperience();

    float getFlightDuration();

    boolean isExperienceCritical();
}
