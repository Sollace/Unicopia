package com.minelittlepony.unicopia.block.zap;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.block.BlockConstructionUtils;
import com.minelittlepony.unicopia.block.TintedBlock;
import com.minelittlepony.unicopia.block.UMapColors;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.*;

public class BaseZapAppleLeavesBlock extends LeavesBlock implements TintedBlock, ZapStagedBlock, ElectrifiedBlock {

    public BaseZapAppleLeavesBlock() {
        super(Settings.create()
                .mapColor(UMapColors.ZAP_LEAVES)
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
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.get(PERSISTENT) || oldState.isOf(state.getBlock())) {
            return;
        }
        updateStage(state, world, pos);
    }

    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);
        if (state.get(PERSISTENT)) {
            return;
        }
        tryAdvanceStage(state, world, pos, random);
    }

    @Override
    public ZapAppleStageStore.Stage getStage(BlockState state) {
        return ZapAppleStageStore.Stage.FLOWERING;
    }

    @Override
    protected final boolean shouldDecay(BlockState state) {
        return false;
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        if (state.get(PERSISTENT)) {
            return Blocks.OAK_LEAVES.calcBlockBreakingDelta(Blocks.OAK_LEAVES.getDefaultState(), player, world, pos);
        }

        float delta = super.calcBlockBreakingDelta(state, player, world, pos);

        if (Pony.of(player).getCompositeRace().canUseEarth()) {
            delta *= 50;
        }

        if (getStage(state) == ZapAppleStageStore.Stage.RIPE) {
            delta *= 5;
        }

        return MathHelper.clamp(delta, 0, 0.9F);
    }

    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        triggerLightning(state, world, pos);
    }

    @Override
    public int getTint(BlockState state, @Nullable BlockRenderView world, @Nullable BlockPos pos, int foliageColor) {
        if (pos == null) {
            return 0x4C7EFA;
        }

        return TintedBlock.blend(TintedBlock.rotate(foliageColor, 2), 0x0000FF, 0.3F);
    }
}
