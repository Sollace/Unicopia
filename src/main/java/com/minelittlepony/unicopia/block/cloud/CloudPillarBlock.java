package com.minelittlepony.unicopia.block.cloud;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class CloudPillarBlock extends CloudBlock {
    private static final MapCodec<CloudPillarBlock> CODEC = Block.createCodec(CloudPillarBlock::new);
    private static final BooleanProperty NORTH = BooleanProperty.of("north");
    private static final BooleanProperty SOUTH = BooleanProperty.of("south");
    private static final Map<Direction, BooleanProperty> DIRECTION_PROPERTIES = Map.of(
            Direction.UP, NORTH,
            Direction.DOWN, SOUTH
    );

    private static final VoxelShape CORE_SHAPE = Block.createCuboidShape(1, 0, 1, 15, 16, 15);
    private static final VoxelShape FOOT_SHAPE = Block.createCuboidShape(0, 0, 0, 16, 5, 16);
    private static final VoxelShape CAP_SHAPE = FOOT_SHAPE.offset(0, 11F / 16F, 0);

    private static final VoxelShape[] SHAPES = new VoxelShape[] {
                          CORE_SHAPE,
        VoxelShapes.union(CORE_SHAPE, FOOT_SHAPE),
        VoxelShapes.union(CORE_SHAPE,             CAP_SHAPE),
        VoxelShapes.union(CORE_SHAPE, FOOT_SHAPE, CAP_SHAPE)
    };
    // [0,0] [0,1]
    // [1,0] [1,1]

    public CloudPillarBlock(Settings settings) {
        super(false, settings);
        setDefaultState(getDefaultState().with(NORTH, true).with(SOUTH, true));
    }

    @Override
    public MapCodec<CloudPillarBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(NORTH, SOUTH);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, EquineContext equineContext) {
        return SHAPES[(state.get(NORTH) ? 0 : 2) + (state.get(SOUTH) ? 0 : 1)];
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(ItemPlacementContext placementContext, EquineContext equineContext) {
        BlockPos pos = placementContext.getBlockPos();
        BlockState state = super.getPlacementState(placementContext, equineContext);
        for (var property : DIRECTION_PROPERTIES.entrySet()) {
            state = state.with(property.getValue(), placementContext.getWorld().getBlockState(pos.offset(property.getKey())).isOf(this));
        }
        return state;
    }

    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis() == Direction.Axis.Y) {
            return state.with(DIRECTION_PROPERTIES.get(direction), neighborState.isOf(this));
        }

        return state;
    }
}
