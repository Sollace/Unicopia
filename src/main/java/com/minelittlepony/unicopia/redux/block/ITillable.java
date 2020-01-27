package com.minelittlepony.unicopia.redux.block;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
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
    BlockState getFarmlandState(ItemStack hoe, PlayerEntity player, World world, BlockState state, BlockPos pos);

    default boolean canBeTilled(ItemStack hoe, PlayerEntity player, World world, BlockState state, BlockPos pos) {
        return world.isAir(pos.up());
    }
}
