package com.minelittlepony.unicopia.ability.magic.spell;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.minecraft.entity.Entity;

/**
 * A spell that's capable of homing in on a pre-defined target.
 */
public interface HomingSpell extends Spell {
    int DEFAULT_RANGE = 600;

    boolean setTarget(Entity target);

    default int getRange(Caster<?> caster) {
        return DEFAULT_RANGE;
    }
}
