package com.minelittlepony.unicopia.gas;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.block.Covering;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.IWorld;

public class CoverableCloudBlock extends CloudBlock {

    public CoverableCloudBlock(GasState variant) {
        super(variant);
        setDefaultState(stateManager.getDefaultState().with(Covering.PROPERTY, Covering.UNCOVERED));
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext context) {
        return getDefaultState().with(Covering.PROPERTY, Covering.getCovering(context.getWorld(), context.getBlockPos().up()));
    }

    @Override
    @Deprecated
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState other, IWorld world, BlockPos pos, BlockPos otherPos) {
        if (direction == Direction.UP) {
            return state.with(Covering.PROPERTY, Covering.getCovering(world, otherPos));
        }

        return state;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Covering.PROPERTY);
    }
}
