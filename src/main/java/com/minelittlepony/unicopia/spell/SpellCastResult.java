package com.minelittlepony.unicopia.spell;

/**
 * A type of action to perform after a spell has completed its handling.
 */
public enum SpellCastResult {
	/**
	 * No action.
	 */
	NONE,
	/**
	 * Place block/gem into the world.
	 */
	PLACE,
	/**
	 * Vanilla behaviour.
	 * In the case of dispensers the item will be ejected into the world.
	 * When right clicking a block the itemstack will be decremented.
	 */
	DEFAULT;
}
