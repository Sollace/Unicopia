package com.minelittlepony.unicopia.core.magic;

public interface IAttachedEffect extends IMagicEffect {
    /**
     * Called every tick when attached to a player.
     *
     * @param source    The entity we are currently attached to.
     * @return true to keep alive
     */
    boolean updateOnPerson(ICaster<?> caster);

    /**
     * Called every tick when attached to a player. Used to apply particle effects.
     * Is only called on the client side.
     *
     * @param source    The entity we are currently attached to.
     */
    default void renderOnPerson(ICaster<?> source) {}
}
