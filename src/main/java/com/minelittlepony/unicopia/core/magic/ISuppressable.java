package com.minelittlepony.unicopia.core.magic;

/**
 * Magic effects that can be suppressed by other nearby effects.
 */
public interface ISuppressable extends IMagicEffect {

    /**
     * Returns true if this spell is currently still suppressed.
     */
    boolean getSuppressed();

    /**
     * Returns true if this spell can be suppressed by the given other spell and caster.
     */
    boolean isVulnerable(ICaster<?> otherSource, IMagicEffect other);

    /**
     * Event triggered when this effect is suppressed.
     */
    void onSuppressed(ICaster<?> otherSource);
}
