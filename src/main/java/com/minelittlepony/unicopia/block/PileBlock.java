package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.Waterloggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class PileBlock extends Block implements Waterloggable {
    public static final int MAX_COUNT = 3;
    public static final IntProperty COUNT = IntProperty.of("count", 1, MAX_COUNT);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;

    public static final VoxelShape[] MYSTERIOUS_EGG_SHAPES = new VoxelShape[] {
            Block.createCuboidShape(3, 0, 3, 13, 13, 13),
            VoxelShapes.union(
                    Block.createCuboidShape(1, 0, 4, 9, 13, 12),
                    Block.createCuboidShape(8, 0, 8, 14, 8, 14)
            ),
            VoxelShapes.union(
                    Block.createCuboidShape(0, 0, -1, 8, 11, 7),
                    Block.createCuboidShape(4, 0, 6, 14, 15, 16),
                    Block.createCuboidShape(10, 0, 0, 16, 8, 6)
            )
    };

    private final VoxelShape[] shapes;

    public PileBlock(Settings settings, VoxelShape[] shapes) {
        super(settings.offset(OffsetType.XZ).dynamicBounds());
        setDefaultState(getDefaultState().with(COUNT, 1));
        this.shapes = shapes;
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        Vec3d offset = state.getModelOffset(world, pos);
        return shapes[state.get(COUNT) - 1].offset(offset.x, offset.y, offset.z);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(WATERLOGGED, COUNT);
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        BlockState state = ctx.getWorld().getBlockState(pos);
        if (state.isOf(this)) {
            return state.with(COUNT, Math.min(MAX_COUNT, state.get(COUNT) + 1));
        }

        return super.getPlacementState(ctx).with(WATERLOGGED, ctx.getWorld().getFluidState(pos).isIn(FluidTags.WATER));
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        pos = pos.down();
        return world.getBlockState(pos).isSideSolid(world, pos, Direction.UP, SideShapeType.CENTER);
    }

    @Deprecated
    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return (!context.shouldCancelInteraction() && context.getStack().isOf(asItem()) && state.get(COUNT) < MAX_COUNT) || super.canReplace(state, context);
    }

    @Deprecated
    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (!state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        if (state.get(WATERLOGGED).booleanValue()) {
            world.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }
}
