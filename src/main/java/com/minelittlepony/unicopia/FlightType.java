package com.minelittlepony.unicopia;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;

public enum FlightType {
    UNSET,
    NONE,
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

    public boolean isPresent() {
        return this != UNSET;
    }

    public boolean canFly() {
        return !isGrounded();
    }

    public boolean canFlyCreative(PlayerEntity player) {
        return player.isCreative() || player.isSpectator();
    }

    public SoundEvent getWingFlapSound() {
        return this == INSECTOID ? USounds.ENTITY_PLAYER_CHANGELING_BUZZ : USounds.ENTITY_PLAYER_PEGASUS_WINGSFLAP;
    }

    public float getWingFlapSoundPitch() {
        return this == INSECTOID ? 0.66F : 1;
    }

    /**
     * Predicate for abilities to control whether a player can fly.
     *
     * This overrides what the race specifies.
     */
    public interface Provider {
        FlightType getFlightType();
    }
}
