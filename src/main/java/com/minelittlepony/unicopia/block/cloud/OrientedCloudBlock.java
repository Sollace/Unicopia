package com.minelittlepony.unicopia.block.cloud;

import com.minelittlepony.unicopia.EquineContext;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.Direction;

public class OrientedCloudBlock extends CloudBlock {
    public static final DirectionProperty FACING = Properties.FACING;

    public OrientedCloudBlock(Settings settings, boolean meltable) {
        super(settings, meltable);
        this.setDefaultState(getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx, EquineContext equineContext) {
        return getDefaultState().with(FACING, ctx.getSide().getOpposite());
    }
}
