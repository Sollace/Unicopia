package com.minelittlepony.unicopia.block.state;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockStateConverter {

    static ReversableBlockStateConverter of(Identifier id) {
        return new StateMapLoader.Indirect<ReversableBlockStateConverter>(id, Optional.empty());
    }

    /**
     * Checks if this collection contains a mapping capable of converting the given state.
     *
     * @param state        State to check
     *
     * @return    True if the state can be converted
     */
    boolean canConvert(@Nullable BlockState state);

    /**
     * Attempts to convert the given state based on the known mappings in this collection.
     *
     * @param world        The world
     * @param state        State to convert
     *
     * @return    The converted state if there is one, otherwise the original state is returned
     */
    @NotNull
    BlockState getConverted(World world, @NotNull BlockState state);

    /**
     * Attempts to convert a block state at a position.
     * Returns true if the block was changed.
     *
     */
    default boolean convert(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);

        if (!canConvert(state)) {
            return false;
        }

        BlockState newState = getConverted(world, state);

        if (state.equals(newState)) {
            return false;
        }

        if (!newState.contains(Properties.DOUBLE_BLOCK_HALF)) {
            world.setBlockState(pos, newState, Block.FORCE_STATE | Block.NOTIFY_LISTENERS);
            return true;
        }

        // for two-tall blocks (like doors) we have to update it's sibling
        boolean lower = newState.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
        BlockPos other = lower ? pos.up() : pos.down();

        if (world.getBlockState(other).isOf(state.getBlock())) {
            world.setBlockState(other, newState.with(Properties.DOUBLE_BLOCK_HALF, lower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER), Block.FORCE_STATE | Block.NOTIFY_LISTENERS);
            world.setBlockState(pos, newState, Block.FORCE_STATE | Block.NOTIFY_LISTENERS);

            return true;
        }

        return false;
    }
}
