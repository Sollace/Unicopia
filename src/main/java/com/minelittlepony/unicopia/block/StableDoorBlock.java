package com.minelittlepony.unicopia.block;

import net.minecraft.block.BlockSetType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldAccess;

public class StableDoorBlock extends DoorBlock {

    public StableDoorBlock(Settings settings, BlockSetType blockSet) {
        super(settings, blockSet);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf half = state.get(HALF);

        if (direction.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (direction == Direction.UP)) {
            if (neighborState.isOf(this) && neighborState.get(HALF) != half) {
                state = state
                        .with(FACING, neighborState.get(FACING))
                        .with(HINGE, neighborState.get(HINGE));
                if (half ==  DoubleBlockHalf.UPPER && direction == Direction.DOWN && !state.get(POWERED)) {
                    state = state.with(OPEN, neighborState.get(OPEN));
                }
                return state;
            }

            return Blocks.AIR.getDefaultState();
        }

        if (half == DoubleBlockHalf.LOWER && direction == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }

        return state;
    }
}
