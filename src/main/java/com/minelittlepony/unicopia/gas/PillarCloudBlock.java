package com.minelittlepony.unicopia.gas;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public class PillarCloudBlock extends CloudBlock {

    public PillarCloudBlock(GasState variant) {
        super(variant);
        setDefaultState(getDefaultState().with(Properties.AXIS, Direction.Axis.Y));
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        switch(rotation) {
        case COUNTERCLOCKWISE_90:
        case CLOCKWISE_90:
            switch(state.get(Properties.AXIS)) {
            case X:
                return state.with(Properties.AXIS, Direction.Axis.Z);
            case Z:
                return state.with(Properties.AXIS, Direction.Axis.X);
            default:
                return state;
            }
        default:
            return state;
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.AXIS);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(Properties.AXIS, ctx.getSide().getAxis());
    }
}
