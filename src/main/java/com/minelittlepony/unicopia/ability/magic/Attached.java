package com.minelittlepony.unicopia.ability.magic;

/**
 * A magic effect that does something when attached to an entity.
 */
public interface Attached extends Spell {
    /**
     * Called every tick when attached to a player.
     *
     * @param source    The entity we are currently attached to.
     * @return true to keep alive
     */
    boolean updateOnPerson(Caster<?> caster);

    /**
     * Called every tick when attached to a player. Used to apply particle effects.
     * Is only called on the client side.
     *
     * @param source    The entity we are currently attached to.
     */
    default void renderOnPerson(Caster<?> source) {}
}
