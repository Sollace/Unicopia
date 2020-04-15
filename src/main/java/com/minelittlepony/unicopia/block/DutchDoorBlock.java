package com.minelittlepony.unicopia.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

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

    // UPPER - HALF/HINGE/POWER{/OPEN}
    // LOWER - HALF/FACING/FACING/OPEN

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction face, BlockState other, IWorld world, BlockPos pos, BlockPos otherPos) {

        // copy properties in stored by the sibling block
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            if (other.getBlock() == this) {
                return state.with(HINGE, other.get(HINGE))
                    .with(POWERED, other.get(POWERED));
            }
        } else {
            if (other.getBlock() == this) {
                return state.with(FACING, other.get(FACING));
            }
        }


        return state;
    }
}
