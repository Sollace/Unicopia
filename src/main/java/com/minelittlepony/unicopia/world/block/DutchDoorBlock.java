package com.minelittlepony.unicopia.world.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public class DutchDoorBlock extends AbstractDoorBlock {

    public DutchDoorBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockPos getPrimaryDoorPos(BlockState state, BlockPos pos) {
        return pos;
    }

    @Override
    protected boolean onPowerStateChanged(World world, BlockState state, BlockPos pos, boolean powered) {
        boolean result = super.onPowerStateChanged(world, state, pos, powered);

        BlockState upper = world.getBlockState(pos.up());
        if (upper.getBlock() == this && upper.get(OPEN) != powered) {
            world.setBlockState(pos.up(), upper.with(OPEN, powered));

            return true;
        }

        return result;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction face, BlockState other, WorldAccess world, BlockPos pos, BlockPos otherPos) {

        DoubleBlockHalf half = state.get(HALF);

        if (face.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (face == Direction.UP)) {
            if (other.getBlock() == this && other.get(HALF) != half) {
                return state
                        .with(FACING, other.get(FACING))
                        .with(HINGE, other.get(HINGE))
                        .with(POWERED, other.get(POWERED));
            }

            return Blocks.AIR.getDefaultState();
        }

        if (half == DoubleBlockHalf.LOWER && face == Direction.DOWN && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }

        return state;
    }
}
