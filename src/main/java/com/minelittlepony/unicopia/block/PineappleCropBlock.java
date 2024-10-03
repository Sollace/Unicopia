package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.compat.seasons.FertilizableUtil;
import com.minelittlepony.unicopia.item.UItems;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class PineappleCropBlock extends CropBlock {
    public static final MapCodec<PineappleCropBlock> CODEC = createCodec(PineappleCropBlock::new);
    public static final EnumProperty<BlockHalf> HALF = Properties.BLOCK_HALF;
    public static final BooleanProperty WILD = BooleanProperty.of("wild");

    public PineappleCropBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(HALF, BlockHalf.BOTTOM).with(WILD, false));
    }

    @Override
    public MapCodec<? extends PineappleCropBlock> getCodec() {
        return CODEC;
    }

    @Override
    public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
        if (state.get(HALF) == BlockHalf.TOP) {
            return UItems.PINEAPPLE.getDefaultStack();
        }
        return UItems.PINEAPPLE_CROWN.getDefaultStack();
    }

    @Override
    public boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(this) || super.canPlantOnTop(floor, world, pos);
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (state.get(WILD) && world.getBlockState(pos.down()).isIn(BlockTags.DIRT)) {
            return world.getBaseLightLevel(pos, 0) >= 8 || world.isSkyVisible(pos);
        }
        return super.canPlaceAt(state, world, pos);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.UP && !neighborState.isOf(this)) {
            return state.with(AGE, Math.min(state.get(AGE), getMaxAge() - 1));
        }
        return super.getStateForNeighborUpdate(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(HALF) == BlockHalf.BOTTOM) {
            super.randomTick(state, world, pos, random);

            BlockState newState = world.getBlockState(pos);

            if (newState.isOf(this) && !isMature(state) && isMature(newState) && world.isAir(pos.up())) {
                world.setBlockState(pos.up(), getDefaultState().with(HALF, BlockHalf.TOP));
            }
        } else {
            int age = getAge(state);
            if (world.getBaseLightLevel(pos, 0) >= 9 && age < getMaxAge()) {
                int steps = FertilizableUtil.getGrowthSteps(world, pos, state, random);
                if (steps > 0) {
                    world.setBlockState(pos, state.with(AGE, Math.min(getMaxAge(), age + steps)), Block.NOTIFY_LISTENERS);
                }
            }
        }
    }

    @Override
    public void applyGrowth(World world, BlockPos pos, BlockState state) {
        world.setBlockState(pos, state.with(AGE, Math.min(getMaxAge(), getAge(state) + getGrowthAmount(world))), Block.NOTIFY_LISTENERS);

        BlockHalf half = state.get(HALF);
        if (half == BlockHalf.BOTTOM && isMature(world.getBlockState(pos))) {
            if (world.isAir(pos.up())) {
                world.setBlockState(pos.up(), getDefaultState().with(HALF, BlockHalf.TOP));
            }
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(HALF, WILD);
    }
}
