package com.minelittlepony.unicopia.block.state;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface BlockStateConverter {
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
    @Nonnull
    BlockState getConverted(World world, @Nonnull BlockState state);

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
            world.setBlockState(pos, newState, 16 | 2);
            return true;
        }

        boolean lower = newState.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
        BlockPos other = lower ? pos.up() : pos.down();

        if (world.getBlockState(other).isOf(state.getBlock())) {
            world.setBlockState(other, newState.with(Properties.DOUBLE_BLOCK_HALF, lower ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER), 16 | 2);
            world.setBlockState(pos, newState, 16 | 2);

            return true;
        }

        return false;
    }
}
