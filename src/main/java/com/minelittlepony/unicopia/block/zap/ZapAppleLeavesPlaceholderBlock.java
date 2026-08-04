package com.minelittlepony.unicopia.block.zap;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore.Stage;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class ZapAppleLeavesPlaceholderBlock extends AirBlock implements ZapStagedBlock {
    public static final IntProperty DISTANCE = Properties.DISTANCE_1_7;

    private static final MapCodec<ZapAppleLeavesPlaceholderBlock> CODEC = createCodec(ZapAppleLeavesPlaceholderBlock::new);

    public ZapAppleLeavesPlaceholderBlock(Settings settings) {
        super(settings);
        setDefaultState(stateManager.getDefaultState().with(DISTANCE, Properties.DISTANCE_0_7_MAX));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public MapCodec<AirBlock> getCodec() {
        return (MapCodec)CODEC;
    }


    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(DISTANCE);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return updateDistanceFromLogs(getDefaultState(), ctx.getWorld(), ctx.getBlockPos());
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return shouldDecay(state);
    }

    @Override
    public Stage getStage(BlockState state) {
        return ZapAppleStageStore.Stage.HIBERNATING;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        updateStage(state, world, pos);
    }

    protected boolean shouldDecay(BlockState state) {
        return state.get(DISTANCE) == 7;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (shouldDecay(state)) {
            dropStacks(state, world, pos);
            world.removeBlock(pos, false);
        }
    }

    @Deprecated
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);
        world.setBlockState(pos, updateDistanceFromLogs(state, world, pos), Block.NOTIFY_ALL);
        tryAdvanceStage(state, world, pos, random);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        int i = getDistanceFromLog(neighborState) + 1;
        if (i != 1 || state.get(DISTANCE) != i) {
            tickView.scheduleBlockTick(pos, this, 1);
        }

        return state;
    }

    private static BlockState updateDistanceFromLogs(BlockState state, WorldAccess world, BlockPos pos) {
        int i = Properties.DISTANCE_0_7_MAX;
        BlockPos.Mutable mutable = new BlockPos.Mutable();

        for (Direction direction : Direction.values()) {
            mutable.set(pos, direction);
            i = Math.min(i, getDistanceFromLog(world.getBlockState(mutable)) + 1);
            if (i == 1) {
                break;
            }
        }

        return state.with(DISTANCE, i);
    }

    private static int getDistanceFromLog(BlockState state) {
        return LeavesBlock.getOptionalDistanceFromLog(state).orElse(7);
    }

}
