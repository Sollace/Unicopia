package com.minelittlepony.unicopia.block;

import java.util.Optional;
import java.util.stream.Stream;

import com.minelittlepony.unicopia.USounds;
import com.minelittlepony.unicopia.ability.EarthPonyGrowAbility;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public class ThornBudBlock extends Block implements EarthPonyGrowAbility.Growable, Fertilizable {
    static final DirectionProperty FACING = Properties.FACING;
    static final int MAX_DISTANCE = 25;
    static final IntProperty DISTANCE = IntProperty.of("distance", 0, MAX_DISTANCE);

    private final BlockState branchState;

    public ThornBudBlock(Settings settings, BlockState branchState) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.DOWN).with(DISTANCE, 0));
        this.branchState = branchState;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, DISTANCE);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextInt(50) == 0) {
            grow(world, state, pos);
        }
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        if (direction == state.get(FACING) && !(neighborState.isOf(this) || neighborState.isOf(branchState.getBlock()))) {
            return Blocks.AIR.getDefaultState();
        }
        return state;
    }

    @Override
    public boolean grow(World world, BlockState state, BlockPos pos) {
        if (state.get(DISTANCE) >= MAX_DISTANCE) {
            return false;
        }
        return pickGrowthDirection(world, state, pos).map(randomDirection -> {
            BlockPos p = pos.offset(randomDirection);

            if (!canReplace(world.getBlockState(p))) {
                return false;
            }

            world.playSound(null, pos, USounds.Vanilla.ITEM_BONE_MEAL_USE, SoundCategory.BLOCKS);
            world.setBlockState(pos, branchState
                    .with(FACING, state.get(FACING))
                    .with(ThornBlock.FACING_PROPERTIES.get(state.get(FACING)), true)
                    .with(ThornBlock.FACING_PROPERTIES.get(randomDirection), true));
            world.setBlockState(p, getDefaultState()
                    .with(FACING, randomDirection.getOpposite())
                    .with(DISTANCE, state.get(DISTANCE) + 1)
            );
            return true;
        }).orElse(false);
    }

    protected boolean canReplace(BlockState state) {
        return state.isReplaceable();
    }

    private static Optional<Direction> pickGrowthDirection(World world, BlockState state, BlockPos pos) {
        Direction excluded = state.get(FACING);
        return Util.getRandomOrEmpty(ThornBlock.FACING_PROPERTIES.keySet().stream()
                .filter(direction -> direction != excluded)
                .flatMap(direction -> getByWeight(direction, excluded))
                .toList(), world.getRandom());
    }

    private static Stream<Direction> getByWeight(Direction input, Direction excluded) {
        return Stream.generate(() -> input)
                .limit(input.getAxis() == excluded.getAxis() ? 6L : input.getAxis() == Direction.Axis.Y ? 1L : 3L);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state, boolean client) {
        return true;
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        grow(world, state, pos);
    }
}
