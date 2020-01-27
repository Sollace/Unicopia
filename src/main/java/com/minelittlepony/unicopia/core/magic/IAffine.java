package com.minelittlepony.unicopia.core.magic;

/**
 * Interface for things that have an affine alignment.
 */
public interface IAffine {
    /**
     * Gets the current alignment.
     * Good/Bad/Neutral
     */
    Affinity getAffinity();
}
