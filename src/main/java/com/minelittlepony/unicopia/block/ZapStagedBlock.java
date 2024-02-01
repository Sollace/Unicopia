package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public interface ZapStagedBlock {
    ZapAppleStageStore.Stage getStage(BlockState state);

    default void updateStage(BlockState state, ServerWorld world, BlockPos pos) {
        ZapAppleStageStore.Stage currentStage = ZapAppleStageStore.get(world).getStage();
        if (currentStage != getStage(state)) {
            state = currentStage.getNewState(state);
            world.setBlockState(pos, state);
        }
        world.scheduleBlockTick(pos, state.getBlock(), 1);
    }

    default void tryAdvanceStage(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        ZapAppleStageStore store = ZapAppleStageStore.get(world);
        ZapAppleStageStore.Stage newStage = store.getStage();
        if (!world.isDay() && getStage(state).mustChangeIntoInstantly(newStage)) {
            state = newStage.getNewState(state);
            world.setBlockState(pos, state);
            onStageChanged(store, newStage, world, state, pos, random);
        }
        world.scheduleBlockTick(pos, state.getBlock(), 1);
    }

    private static void onStageChanged(ZapAppleStageStore store, ZapAppleStageStore.Stage stage, ServerWorld world, BlockState state, BlockPos pos, Random random) {
        boolean mustFruit = Random.create(state.getRenderingSeed(pos)).nextInt(5) < 2;
        BlockState below = world.getBlockState(pos.down());

        if (world.isAir(pos.down())) {
            if (stage == ZapAppleStageStore.Stage.FRUITING && mustFruit) {
                world.setBlockState(pos.down(), UBlocks.ZAP_BULB.getDefaultState(), Block.NOTIFY_ALL);
                store.triggerLightningStrike(pos);
            }
        }

        if (stage != ZapAppleStageStore.Stage.HIBERNATING && world.getRandom().nextInt(10) == 0) {
            store.triggerLightningStrike(pos);
        }

        if (stage == ZapAppleStageStore.Stage.RIPE) {
            if (below.isOf(UBlocks.ZAP_BULB)) {
                world.setBlockState(pos.down(), UBlocks.ZAP_APPLE.getDefaultState(), Block.NOTIFY_ALL);
                store.playMoonEffect(pos);
            }
        }

        if (mustFruit && stage == ZapAppleStageStore.Stage.HIBERNATING) {
            if (below.isOf(UBlocks.ZAP_APPLE) || below.isOf(UBlocks.ZAP_BULB)) {
                world.setBlockState(pos.down(), Blocks.AIR.getDefaultState());
            }
        }
    }
}
