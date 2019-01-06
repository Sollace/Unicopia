package com.minelittlepony.unicopia.block;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Blocks that can be turned into farmland when tilled.
 */
public interface ITillable {
    /**
     * Gets the farmland/tilled state for this block when attacked by a hoe.
     */
    @Nonnull
    IBlockState getFarmlandState(ItemStack hoe, EntityPlayer player, World world, IBlockState state, BlockPos pos);

    default boolean canBeTilled(ItemStack hoe, EntityPlayer player, World world, IBlockState state, BlockPos pos) {
        return world.isAirBlock(pos.up());
    }
}
