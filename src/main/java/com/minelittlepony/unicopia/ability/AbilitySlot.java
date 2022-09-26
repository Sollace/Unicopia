package com.minelittlepony.unicopia.ability;

public enum AbilitySlot {
    NONE,
    /**
     * The primary ability. Corresponds to the largest ring in the HUD
     */
    PRIMARY,
    /**
     * THe secondary ability. Corresponds to the top small ring in the HUD
     */
    SECONDARY,
    /**
     * The tertiary ability. Corresponds to the bottom small ring in the HUD.
     */
    TERTIARY,
    /**
     * The passive primary ability. Appears in place of the primary ability whilst sneaking.
     */
    PASSIVE;

    public boolean isPassive() {
        return this == PASSIVE;
    }
}
