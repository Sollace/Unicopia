package com.minelittlepony.unicopia.ability.magic;

/**
 * Interface for things that have an affine alignment.
 */
public interface Affine {
    /**
     * Gets the current alignment.
     * Good/Bad/Neutral
     */
    Affinity getAffinity();
}
