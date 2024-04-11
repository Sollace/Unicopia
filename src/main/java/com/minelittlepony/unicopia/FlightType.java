package com.minelittlepony.unicopia;

import java.util.Locale;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.StringIdentifiable;

public enum FlightType implements StringIdentifiable {
    UNSET,
    NONE,
    AVIAN,
    INSECTOID,
    ARTIFICIAL;

    @SuppressWarnings("deprecation")
    public static final EnumCodec<FlightType> CODEC = StringIdentifiable.createCodec(FlightType::values);

    private final String name = name().toLowerCase(Locale.ROOT);

    @Override
    public String asString() {
        return name;
    }

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

    public FlightType or(FlightType other) {
        return ordinal() > other.ordinal() ? this : other;
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
