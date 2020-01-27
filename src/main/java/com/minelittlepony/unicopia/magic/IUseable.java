package com.minelittlepony.unicopia.magic;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.magic.spells.CastResult;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.world.World;

/**
 * Interface for right-click actions.
 *
 */
public interface IUseable {

    /**
     * Triggered when the player right clicks a block
     *
     * @param stack        The current itemstack
     * @param affinity  The affinity of the casting artifact
     * @param player    The player
     * @param world        The player's world
     * @param pos        The location clicked
     * @param side        The side of the block clicked
     * @param hitX        X offset inside the block
     * @param hitY        Y offset inside the block
     * @param hitZ        Z offset inside the block
     *
     * @return    ActionResult for the type of action to perform
     */
    CastResult onUse(ItemUsageContext context, Affinity affinity);

    /**
     * Triggered when the player right clicks
     *
     * @param stack        The current itemstack
     * @param affinity  The affinity of the casting artifact
     * @param player    The player
     * @param world        The player's world
     * @param hitEntity    The entity in focus, if any
     *
     * @return    ActionResult for the type of action to perform
     */
    CastResult onUse(ItemStack stack, Affinity affinity, PlayerEntity player, World world, @Nullable Entity hitEntity);
}
