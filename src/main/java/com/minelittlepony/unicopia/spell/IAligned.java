package com.minelittlepony.unicopia.spell;

/**
 * Interface for things that have an affine alignment.
 */
public interface IAligned {
    /**
     * Gets the current alignment.
     * Good/Bad/Neutral
     */
    SpellAffinity getAffinity();
}
