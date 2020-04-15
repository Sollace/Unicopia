package com.minelittlepony.unicopia.magic;

/**
 * Magic effects that can be suppressed by other nearby effects.
 */
public interface SuppressableEffect extends MagicEffect {

    /**
     * Returns true if this spell is currently still suppressed.
     */
    boolean getSuppressed();

    /**
     * Returns true if this spell can be suppressed by the given other spell and caster.
     */
    boolean isVulnerable(Caster<?> otherSource, MagicEffect other);

    /**
     * Event triggered when this effect is suppressed.
     */
    void onSuppressed(Caster<?> otherSource);
}
