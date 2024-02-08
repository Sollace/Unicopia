package com.minelittlepony.unicopia.block.zap;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import com.minelittlepony.unicopia.server.world.ZapAppleStageStore.Stage;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class ZapAppleLeavesPlaceholderBlock extends AirBlock implements ZapStagedBlock {
    private static final MapCodec<ZapAppleLeavesPlaceholderBlock> CODEC = createCodec(ZapAppleLeavesPlaceholderBlock::new);

    public ZapAppleLeavesPlaceholderBlock(Settings settings) {
        super(settings);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public MapCodec<AirBlock> getCodec() {
        return (MapCodec)CODEC;
    }

    @Override
    public Stage getStage(BlockState state) {
        return ZapAppleStageStore.Stage.HIBERNATING;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        updateStage(state, world, pos);
    }

    @Deprecated
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);
        tryAdvanceStage(state, world, pos, random);
    }
}
