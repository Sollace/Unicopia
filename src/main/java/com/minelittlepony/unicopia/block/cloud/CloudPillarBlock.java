package com.minelittlepony.unicopia.block.cloud;

import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class CloudPillarBlock extends CloudBlock {
    public static final EnumProperty<Direction.Axis> AXIS = Properties.AXIS;
    private static final BooleanProperty TOP = Properties.NORTH;
    private static final BooleanProperty BOTTOM = Properties.SOUTH;
    private static final Map<Direction, BooleanProperty> DIRECTION_PROPERTIES = Map.of(
            Direction.UP, TOP, Direction.DOWN, BOTTOM,
            Direction.SOUTH, TOP, Direction.NORTH, BOTTOM,
            Direction.EAST, TOP, Direction.WEST, BOTTOM
    );
    private static final Function<Direction.Axis, VoxelShape[]> SHAPES = Util.memoize(axis -> {
        int[] offsets = { axis.choose(1, 0, 0), axis.choose(0, 1, 0), axis.choose(0, 0, 1) };
        float capOffset = 11F / 16F;
        VoxelShape core = Block.createCuboidShape(
            axis.choose(0, 1, 1), axis.choose(1, 0, 1), axis.choose(1, 1, 0),
            16 - axis.choose(0, 1, 1), 16 - axis.choose(1, 0, 1), 16 - axis.choose(1, 1, 0)
        );
        VoxelShape foot = Block.createCuboidShape(0, 0, 0, 16 - (11 * offsets[0]), 16 - (11 * offsets[1]), 16 - (11 * offsets[2]));
        VoxelShape cap = foot.offset(capOffset * offsets[0], capOffset * offsets[1], capOffset * offsets[2]);
        return new VoxelShape[] {
                core,
                VoxelShapes.union(core, foot),
                VoxelShapes.union(core, cap),
                VoxelShapes.union(core, cap, foot)
        };
    });

    public CloudPillarBlock(Settings settings) {
        super(settings, false);
        setDefaultState(getDefaultState().with(TOP, true).with(BOTTOM, true).with(AXIS, Direction.Axis.Y));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS, TOP, BOTTOM);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, EquineContext equineContext) {
        var axis = state.get(AXIS);

        int[] offsets = { axis.choose(1, 0, 0), axis.choose(0, 1, 0), axis.choose(0, 0, 1) };
        float capOffset = 11F / 16F;
        VoxelShape core = Block.createCuboidShape(
                axis.choose(0, 1, 1), axis.choose(1, 0, 1), axis.choose(1, 1, 0),
                16 - axis.choose(0, 1, 1), 16 - axis.choose(1, 0, 1), 16 - axis.choose(1, 1, 0)
            );
        VoxelShape foot = Block.createCuboidShape(0, 0, 0, 16 - (11 * offsets[0]), 16 - (11 * offsets[1]), 16 - (11 * offsets[2]));
        VoxelShape cap = foot.offset(capOffset * offsets[0], capOffset * offsets[1], capOffset * offsets[2]);
        var temp = new VoxelShape[] {
                core,
                VoxelShapes.union(core, foot),
                VoxelShapes.union(core, cap),
                VoxelShapes.union(core, cap, foot)
        };
        return temp[(state.get(TOP) ? 0 : 2) + (state.get(BOTTOM) ? 0 : 1)];
        //return SHAPES.apply(state.get(AXIS))[(state.get(TOP) ? 0 : 2) + (state.get(BOTTOM) ? 0 : 1)];
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(ItemPlacementContext placementContext, EquineContext equineContext) {
        BlockPos pos = placementContext.getBlockPos();
        Direction.Axis axis = placementContext.getSide().getAxis();
        Direction upDirection = Direction.get(AxisDirection.POSITIVE, axis);
        Direction downDirection = Direction.get(AxisDirection.NEGATIVE, axis);
        BlockState above = placementContext.getWorld().getBlockState(pos.offset(upDirection));
        BlockState below = placementContext.getWorld().getBlockState(pos.offset(downDirection));
        return super.getPlacementState(placementContext, equineContext)
                .with(DIRECTION_PROPERTIES.get(upDirection), above.isOf(this) && above.get(AXIS) == axis)
                .with(DIRECTION_PROPERTIES.get(downDirection), below.isOf(this) && below.get(AXIS) == axis)
                .with(AXIS, axis);
    }

    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis() == state.get(AXIS)) {
            return state.with(DIRECTION_PROPERTIES.get(direction), neighborState.isOf(this) && neighborState.get(AXIS) == state.get(AXIS));
        }

        return state;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return PillarBlock.changeRotation(state, rotation);
    }
}
