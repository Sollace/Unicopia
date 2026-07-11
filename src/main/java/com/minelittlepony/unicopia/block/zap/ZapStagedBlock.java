package com.minelittlepony.unicopia.block.zap;

import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public interface ZapStagedBlock {
    ZapAppleStageStore.Stage getStage(BlockState state);

    default void updateStage(BlockState state, World world, BlockPos pos) {
        if (!(world instanceof ServerWorld sw)) {
            return;
        }
        ZapAppleStageStore.Stage currentStage = ZapAppleStageStore.get(sw).getStage();
        if (currentStage != getStage(state)) {
            state = getState(currentStage);
            changeLeavesState(currentStage, world, state, pos);
        }
        world.scheduleBlockTick(pos, state.getBlock(), 1);
    }

    default void tryAdvanceStage(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        ZapAppleStageStore store = ZapAppleStageStore.get(world);
        ZapAppleStageStore.Stage currentStage = store.getStage();
        if (!world.isDay() && currentStage != getStage(state)) {
            int transitionRate = getTransitionRate(currentStage);
            if (transitionRate == 0 || random.nextInt(transitionRate) == 0) {
                state = getState(currentStage);
                changeLeavesState(currentStage, world, state, pos);
                onStageChanged(store, currentStage, world, state, pos, random);
            }
        }
        world.scheduleBlockTick(pos, state.getBlock(), 1);
    }

    default int getTransitionRate(ZapAppleStageStore.Stage stage) {
        if (stage == ZapAppleStageStore.Stage.HIBERNATING || stage == ZapAppleStageStore.Stage.GREENING) {
            return 10;
        }
        if (stage == ZapAppleStageStore.Stage.RIPE) {
            return 300;
        }
        return 1500;
    }

    default BlockState getState(ZapAppleStageStore.Stage stage) {
        if (stage == ZapAppleStageStore.Stage.HIBERNATING) {
            return UBlocks.ZAP_LEAVES_PLACEHOLDER.getDefaultState();
        }
        if (stage == ZapAppleStageStore.Stage.FLOWERING) {
            return UBlocks.FLOWERING_ZAP_LEAVES.getDefaultState();
        }
        return UBlocks.ZAP_LEAVES.getDefaultState().with(ZapAppleLeavesBlock.STAGE, stage);
    }

    private static void onStageChanged(ZapAppleStageStore store, ZapAppleStageStore.Stage stage, ServerWorld world, BlockState state, BlockPos pos, Random random) {
        BlockPos down = pos.down();
        BlockState below = world.getBlockState(pos.down());

        if (stage == ZapAppleStageStore.Stage.FRUITING && world.isAir(down)) {
            if (Random.create(state.getRenderingSeed(pos)).nextInt(5) < 2) {
                world.setBlockState(down, UBlocks.ZAP_BULB.getDefaultState(), Block.NOTIFY_ALL);
                store.triggerLightningStrike(pos);
            }
        }

        if (stage != ZapAppleStageStore.Stage.HIBERNATING && world.getRandom().nextInt(10) == 0) {
            store.triggerLightningStrike(pos);
        }

        if (stage == ZapAppleStageStore.Stage.RIPE && below.isOf(UBlocks.ZAP_BULB)) {
            world.setBlockState(down, UBlocks.ZAP_APPLE.getDefaultState(), Block.NOTIFY_ALL);
            store.playMoonEffect(pos);
        }

        if (stage == ZapAppleStageStore.Stage.HIBERNATING && (below.isOf(UBlocks.ZAP_APPLE) || below.isOf(UBlocks.ZAP_BULB))) {
            world.breakBlock(down, false);
        }
    }

    private static void changeLeavesState(ZapAppleStageStore.Stage stage, World world, BlockState state, BlockPos pos) {
        BlockPos down = pos.down();
        BlockState below = world.getBlockState(down);
        if (stage == ZapAppleStageStore.Stage.HIBERNATING && (below.isOf(UBlocks.ZAP_APPLE) || below.isOf(UBlocks.ZAP_BULB))) {
            world.breakBlock(down, false);
        }
        world.setBlockState(pos, state);
    }
}
