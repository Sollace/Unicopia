package com.minelittlepony.unicopia.ability.magic;

import com.minelittlepony.unicopia.ability.magic.spell.Spell;

/**
 * Magic effects that can be suppressed by other nearby effects.
 */
public interface IllusionarySpell extends Spell {

    /**
     * Returns true if this spell is currently still suppressed.
     */
    boolean isSuppressed();

    /**
     * Returns true if this illusion can be suppressed by the given other spell and caster.
     */
    boolean isVulnerable(Caster<?> otherSource, Spell other);

    /**
     * Event triggered when this effect is suppressed.
     */
    void onSuppressed(Caster<?> otherSource, float suppressTime);
}
