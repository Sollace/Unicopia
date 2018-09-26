package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.InbtSerialisable;

/**
 * Interface for a magic spells
 */
public interface IMagicEffect extends InbtSerialisable, ILevelled {

	String getName();

	/**
	 * Sets this effect as dead.
	 */
	void setDead();

	/**
	 * Returns true if this spell is dead, and must be cleaned up.
	 */
	boolean getDead();

	/**
	 * Called every tick when attached to a player.
	 *
	 * @param source	The entity we are currently attached to.
	 * @return true to keep alive
	 */
    default boolean updateOnPerson(ICaster<?> caster) {
        return update(caster, getCurrentLevel());
    }

	/**
	 * Called every tick when attached to a gem.
	 *
	 * @param source   The entity we are currently attached to.
	 * @param level		Current active spell level
	 */
	boolean update(ICaster<?> source, int level);

    /**
     * Called every tick when attached to a player. Used to apply particle effects.
     * Is only called on the client side.
     *
     * @param source    The entity we are currently attached to.
     */
    default void renderOnPerson(ICaster<?> source) {
        render(source, getCurrentLevel());
    }

	/**
	 * Called every tick when attached to an entity to produce particle effects.
	 * Is only called on the client side.
	 *
	 * @param source	The entity we are attached to.
	 * @param level		Current spell level
	 */
	void render(ICaster<?> source, int level);

	/**
	 * Return true to allow the gem update and move.
	 */
	default boolean allowAI() {
	    return false;
	}
}
