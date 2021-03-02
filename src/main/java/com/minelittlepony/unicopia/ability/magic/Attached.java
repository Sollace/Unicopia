package com.minelittlepony.unicopia.ability.magic;

/**
 * A magic effect that does something when attached to an entity.
 */
public interface Attached extends Spell {
    /**
     * Called every tick when attached to a living entity.
     *
     * @param source    The entity we are currently attached to.
     * @return true to keep alive
     */
    boolean onBodyTick(Caster<?> source);
}
