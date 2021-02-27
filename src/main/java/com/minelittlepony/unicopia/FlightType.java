package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.sound.SoundEvent;

public enum FlightType {
    NONE,
    CREATIVE,
    AVIAN,
    INSECTOID,
    ARTIFICIAL;

    public boolean isGrounded() {
        return this == NONE;
    }

    public boolean isAvian() {
        return this == AVIAN || isArtifical();
    }

    public boolean isArtifical() {
        return this == ARTIFICIAL;
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

    public SoundEvent getWingFlapSound() {
        return this == INSECTOID ? USounds.ENTITY_PLAYER_CHANGELING_BUZZ : USounds.ENTITY_PLAYER_PEGASUS_WINGSFLAP;
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
