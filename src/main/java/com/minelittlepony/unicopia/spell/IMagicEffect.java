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
	boolean update(ICaster<?> caster);

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
    default void render(ICaster<?> source) {

    }

	/**
	 * Called every tick when attached to an entity to produce particle effects.
	 * Is only called on the client side.
	 *
	 * @param source	The entity we are attached to.
	 * @param level		Current spell level
	 */
	default void render(ICaster<?> source, int level) {

	}

	/**
	 * Return true to allow the gem update and move.
	 */
	default boolean allowAI() {
	    return false;
	}
}
