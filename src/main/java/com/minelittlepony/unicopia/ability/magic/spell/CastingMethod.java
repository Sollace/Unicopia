package com.minelittlepony.unicopia.ability.magic.spell;

public enum CastingMethod {
    /**
     * Casting from a gem or a unicorn's equipped spell.
     */
    DIRECT,
    /**
     * Casting a projectile form from a gem or unicorn's equipped spell
     */
    STORED,
    /**
     * Result of a projectile impact
     */
    INDIRECT,
    /**
     * Casting from a magic staff
     */
    STAFF,
    /**
     * Result of an entities innate ability
     */
    INNATE;

    public boolean isIndirectCause() {
        return this == STAFF || this == STORED;
    }

    public boolean isIndirectEffect() {
        return this == INDIRECT;
    }

    public boolean isDirect() {
        return this == DIRECT || this == INNATE;
    }

    public boolean isTool() {
        return this == STAFF;
    }
}