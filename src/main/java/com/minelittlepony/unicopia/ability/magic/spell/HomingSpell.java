package com.minelittlepony.unicopia.ability.magic.spell;

import net.minecraft.entity.Entity;

/**
 * A spell that's capable of homing in on a pre-defined target.
 */
public interface HomingSpell extends Spell {
    boolean setTarget(Entity target);
}
