package com.minelittlepony.unicopia.entity.player;

/**
 * Interface for controlling flight.
 */
public interface Motion {
    /**
     * True if we're currently flying.
     */
    boolean isFlying();

    boolean isGliding();

    boolean isDiving();

    boolean isRainbooming();

    float getWingAngle();

    PlayerDimensions getDimensions();
}
