package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.entity.player.Pony;

public enum FlightType {
    NONE,
    CREATIVE,
    AVIAN,
    INSECTOID;

    public boolean isGrounded() {
        return this == NONE;
    }

    public boolean canFly() {
        return !isGrounded();
    }

    public boolean canFlyCreative() {
        return this == CREATIVE || this == INSECTOID;
    }

    public boolean canFlySurvival() {
        return canFly() && !canFlyCreative();
    }

    /**
     * Predicate for abilities to control whether a player can fly.
     *
     * This overrides what the race specifies.
     */
    public interface Provider {
        FlightType getFlightType(Pony player);
    }
}
