package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;

public class BaseZapAppleLeavesBlock extends LeavesBlock implements TintedBlock {

    BaseZapAppleLeavesBlock() {
        super(Settings.of(Material.LEAVES)
                .strength(500, 1200)
                .ticksRandomly()
                .sounds(BlockSoundGroup.AZALEA_LEAVES)
                .nonOpaque()
                .allowsSpawning(BlockConstructionUtils::canSpawnOnLeaves)
                .suffocates(BlockConstructionUtils::never)
                .blockVision(BlockConstructionUtils::never)
        );
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return !state.get(PERSISTENT);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        tryAdvanceStage(state, world, pos, random);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);
        tryAdvanceStage(state, world, pos, random);
        world.scheduleBlockTick(pos, this, 1);
    }

    private void tryAdvanceStage(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(PERSISTENT)) {
            return;
        }

        ZapAppleStageStore store = ZapAppleStageStore.get(world);
        ZapAppleStageStore.Stage newStage = store.getStage();
        if (!world.isDay() && getStage(state).mustChangeIntoInstantly(newStage)) {
            world.setBlockState(pos, newStage.getNewState(state));
            onStageChanged(store, newStage, world, state, pos, random);
        }
    }

    protected ZapAppleStageStore.Stage getStage(BlockState state) {
        return ZapAppleStageStore.Stage.FLOWERING;
    }

    @Override
    protected boolean shouldDecay(BlockState state) {
        return false;
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (state.get(PERSISTENT)) {
            return Blocks.OAK_LEAVES.calcBlockBreakingDelta(Blocks.OAK_LEAVES.getDefaultState(), player, world, pos);
        }

        float delta = super.calcBlockBreakingDelta(state, player, world, pos);

        if (Pony.of(player).getSpecies().canUseEarth()) {
            delta *= 50;
        }

        if (getStage(state) == ZapAppleStageStore.Stage.RIPE) {
            delta *= 5;
        }

        return MathHelper.clamp(delta, 0, 0.9F);
    }

    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        ZapBlock.triggerLightning(state, world, pos, player);
    }

    @Override
    public int getTint(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int foliageColor) {

        if (pos == null) {
            return 0x4C7EFA;
        }

        return TintedBlock.blend(TintedBlock.rotate(foliageColor, 2), 0x0000FF, 0.3F);
    }

    static void onStageChanged(ZapAppleStageStore store, ZapAppleStageStore.Stage stage, ServerWorld world, BlockState state, BlockPos pos, Random random) {
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
