package com.minelittlepony.unicopia.spell;

import javax.annotation.Nonnull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

/**
 * Interface for right-click actions.
 *
 */
public interface IUseAction {

	/**
	 * Triggered when the player right clicks a block
	 *
	 * @param stack		The current itemstack
	 * @param affinity  The affinity of the casting artifact
	 * @param player	The player
	 * @param world		The player's world
	 * @param pos		The location clicked
	 * @param side		The side of the block clicked
	 * @param hitX		X offset inside the block
	 * @param hitY		Y offset inside the block
	 * @param hitZ		Z offset inside the block
	 *
	 * @return	ActionResult for the type of action to perform
	 */
	SpellCastResult onUse(ItemStack stack, SpellAffinity affinity, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ);

	/**
	 * Triggered when the player right clicks
	 *
	 * @param stack		The current itemstack
	 * @param affinity  The affinity of the casting artifact
	 * @param player	The player
	 * @param world		The player's world
	 * @param hitEntity	The entity in focus, if any
	 *
	 * @return	ActionResult for the type of action to perform
	 */
	SpellCastResult onUse(ItemStack stack, SpellAffinity affinity, EntityPlayer player, World world, @Nonnull Entity hitEntity);
}
