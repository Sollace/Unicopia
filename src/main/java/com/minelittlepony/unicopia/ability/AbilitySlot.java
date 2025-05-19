package com.minelittlepony.unicopia.ability;

public enum AbilitySlot {
    /**
     * No slot. Corresponds to abilities that are not equipped.
     */
    NONE,
    /**
     * The primary ability slot. Corresponds to the largest ring in the HUD
     */
    PRIMARY,
    /**
     * The secondary ability slot. Corresponds to the top small ring in the HUD
     */
    SECONDARY,
    /**
     * The tertiary ability slot. Corresponds to the bottom small ring in the HUD.
     */
    TERTIARY,
    /**
     * The passive primary ability slot. Appears in place of the primary ability whilst sneaking.
     */
    PASSIVE;

    public boolean isPassive() {
        return this == PASSIVE;
    }
}
