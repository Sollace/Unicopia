package com.minelittlepony.unicopia.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.IWorld;
import net.minecraft.world.WorldView;

public class WallGemTorchBlock extends GemTorchBlock {
    private static final VoxelShape NORTH = createCuboidShape(6, 3, 10, 10, 16, 16);
    private static final VoxelShape SOUTH = createCuboidShape(6, 3, 0, 10, 16, 5);
    private static final VoxelShape WEST  = createCuboidShape(10, 3, 6, 16, 16, 10);
    private static final VoxelShape EAST  = createCuboidShape(0, 3, 6, 5, 16, 10);

    public WallGemTorchBlock(Settings settings) {
        super(settings);
        setDefaultState(stateManager.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, Direction.NORTH)
                .with(Properties.LIT, true)
        );
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
       BlockState blockState = Blocks.WALL_TORCH.getPlacementState(ctx);

       if (blockState != null) {
           return getDefaultState().with(Properties.HORIZONTAL_FACING, blockState.get(Properties.HORIZONTAL_FACING));
       }

       return null;
    }

    @Override
    protected Direction getDirection(BlockState state) {
        return state.get(Properties.HORIZONTAL_FACING);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
       return Blocks.WALL_TORCH.canPlaceAt(state, world, pos);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction facing, BlockState neighborState, IWorld world, BlockPos pos, BlockPos neighborPos) {
       return Blocks.WALL_TORCH.getStateForNeighborUpdate(state, facing, neighborState, world, pos, neighborPos);
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
       return Blocks.WALL_TORCH.rotate(state, rotation);
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
       return Blocks.WALL_TORCH.mirror(state, mirror);
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView view, BlockPos pos, Direction facing) {
       return state.get(Properties.LIT) && state.get(Properties.HORIZONTAL_FACING) != facing ? 12 : 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.HORIZONTAL_FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView source, BlockPos pos, EntityContext context) {
        switch (state.get(Properties.HORIZONTAL_FACING)) {
            case EAST: return EAST;
            case WEST: return WEST;
            case SOUTH: return SOUTH;
            case NORTH: return NORTH;
            default: return super.getOutlineShape(state, source, pos, context);
        }
    }
}
