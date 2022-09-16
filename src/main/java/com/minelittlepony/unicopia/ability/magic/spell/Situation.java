package com.minelittlepony.unicopia.ability.magic.spell;

/**
 * The situation of the spell being ticked.
 */
public enum Situation {
    /**
     * Ticks coming from living entities like the player and animals.
     */
    BODY,
    /**
     * Ticks coming from flying projectiles.
     */
    PROJECTILE,
    /**
     * Ticks coming from a PlaceableSpell that's being updated as a ground entity.
     */
    GROUND,
    /**
     * Ticks coming directly from the ground SpellCastEntity.
     * Handled only by PlaceableSpell. No other spell should receive this.
     */
    GROUND_ENTITY
}
