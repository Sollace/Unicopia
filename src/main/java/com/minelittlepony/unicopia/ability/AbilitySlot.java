package com.minelittlepony.unicopia.ability;

public enum AbilitySlot {
    NONE,
    PRIMARY,
    SECONDARY,
    TERTIARY,
    PASSIVE;

    public boolean isPassive() {
        return this == PASSIVE;
    }
}
