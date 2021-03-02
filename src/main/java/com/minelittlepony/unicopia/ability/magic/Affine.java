package com.minelittlepony.unicopia.ability.magic;

import com.minelittlepony.unicopia.Affinity;

/**
 * Interface for things that have an affine alignment.
 */
public interface Affine {
    /**
     * Gets the current alignment.
     * Good/Bad/Neutral
     */
    Affinity getAffinity();

    default boolean isEnemy(Affine other) {
        return !getAffinity().alignsWith(other.getAffinity());
    }

    default boolean isFriendlyTogether(Affine other) {
        return getAffinity() != Affinity.BAD && other.getAffinity() != Affinity.BAD;
    }
}
