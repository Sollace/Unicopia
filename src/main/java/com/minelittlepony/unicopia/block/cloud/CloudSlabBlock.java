package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.EquineContext;
import com.minelittlepony.unicopia.util.serialization.CodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class CloudSlabBlock extends WaterloggableCloudBlock {
    private static final MapCodec<WaterloggableCloudBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.fieldOf("meltable").forGetter(b -> b.meltable),
            CodecUtils.supplierOf(Soakable.CODEC).optionalFieldOf("soggy_block", null).forGetter(b -> b.soggyBlock),
            BedBlock.createSettingsCodec()
    ).apply(instance, WaterloggableCloudBlock::new));
    private static final VoxelShape BOTTOM_SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);
    private static final VoxelShape TOP_SHAPE = Block.createCuboidShape(0.0, 8.0, 0.0, 16.0, 16.0, 16.0);

    public CloudSlabBlock(boolean meltable, @Nullable Supplier<Soakable> soggyBlock, Settings settings) {
        super(meltable, soggyBlock, settings);
        setDefaultState(getDefaultState().with(SlabBlock.TYPE, SlabType.BOTTOM));
    }

    @Override
    public MapCodec<? extends WaterloggableCloudBlock> getCodec() {
        return CODEC;
    }

    @Override
    public boolean hasSidedTransparency(BlockState state) {
        return state.get(SlabBlock.TYPE) != SlabType.DOUBLE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(SlabBlock.TYPE);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context, EquineContext equineContext) {
        return switch (state.get(SlabBlock.TYPE)) {
            case DOUBLE -> VoxelShapes.fullCube();
            case TOP -> TOP_SHAPE;
            case BOTTOM -> BOTTOM_SHAPE;
        };
    }

    @Override
    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx, EquineContext equineContext) {
        BlockPos blockPos = ctx.getBlockPos();
        BlockState blockState = ctx.getWorld().getBlockState(blockPos);
        if (blockState.isOf(this)) {
            return blockState.with(SlabBlock.TYPE, SlabType.DOUBLE).with(WATERLOGGED, false);
        }
        BlockState state = super.getPlacementState(ctx, equineContext).with(SlabBlock.TYPE, SlabType.BOTTOM);
        Direction direction = ctx.getSide();
        if (direction == Direction.DOWN || direction != Direction.UP && ctx.getHitPos().y - blockPos.getY() > 0.5) {
            return state.with(SlabBlock.TYPE, SlabType.TOP);
        }
        return state;
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context, EquineContext equineContext) {
        SlabType slabType = state.get(SlabBlock.TYPE);
        if (slabType == SlabType.DOUBLE || !context.getStack().isOf(asItem())) {
            return false;
        }

        if (!context.canReplaceExisting()) {
            return true;
        }

        boolean hitTop = context.getHitPos().y - context.getBlockPos().getY() > 0.5;
        Direction side = context.getSide();
        if (slabType == SlabType.BOTTOM) {
            return side == Direction.UP || hitTop && side.getAxis().isHorizontal();
        }
        return side == Direction.DOWN || !hitTop && side.getAxis().isHorizontal();
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        return state.get(SlabBlock.TYPE) != SlabType.DOUBLE && super.tryFillWithFluid(world, pos, state, fluidState);
    }

    @Override
    public boolean canFillWithFluid(PlayerEntity player, BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return state.get(SlabBlock.TYPE) != SlabType.DOUBLE && super.canFillWithFluid(player, world, pos, state, fluid);
    }
}
