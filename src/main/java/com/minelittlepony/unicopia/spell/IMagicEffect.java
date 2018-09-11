package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.InbtSerialisable;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

/**
 *
 * Interface for a magic spell
 *
 */
public interface IMagicEffect extends InbtSerialisable {

	/**
	 * Maximum level this spell can reach or -1 for unlimited.
	 * <br>
	 * If a gem goes past this level it is more likely to explode.
	 */
	default int getMaxLevel() {
	    return 0;
	}

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
	boolean update(Entity source);

	/**
	 * Called every tick when attached to a player. Used to apply particle effects.
	 * Is only called on the client side.
	 *
	 * @param source	The entity we are currently attached to.
	 */
	default void render(Entity source) {

	}

	/**
	 * Called every tick when attached to a gem.
	 *
	 * @param source	The entity we are attached to.
	 * @param w			The world
	 * @param x			Entity position x
	 * @param y			Entity position y
	 * @param z			Entity position z
	 * @param level		Current spell level
	 */
	boolean updateAt(ICaster<?> source, World w, double x, double y, double z, int level);

	/**
	 * Called every tick when attached to an entity to produce particle effects.
	 * Is only called on the client side.
	 *
	 * @param source	The entity we are attached to.
	 * @param w			The world
	 * @param x			Entity position x
	 * @param y			Entity position y
	 * @param z			Entity position z
	 * @param level		Current spell level
	 */
	default void renderAt(ICaster<?> source, World w, double x, double y, double z, int level) {

	}

	/**
	 * Return true to allow the gem update and move.
	 */
	default boolean allowAI() {
	    return false;
	}
}
