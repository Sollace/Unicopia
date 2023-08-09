package com.minelittlepony.unicopia.ability.magic.spell;

public enum CastingMethod {
    /**
     * Casting from a gem or a unicorn's equipped spell.
     */
    GEM,
    /**
     * Casting a projectile form from a gem or unicorn's equipped spell
     */
    GEM_PROJECTILE,
    /**
     * Result of a projectile impact
     */
    PROJECTILE,
    /**
     * Casting from a magic staff
     */
    STAFF,
    /**
     * Result of an entities innate ability
     */
    INNATE
}