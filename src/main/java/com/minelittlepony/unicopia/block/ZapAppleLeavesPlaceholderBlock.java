package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;

public class ZapAppleLeavesPlaceholderBlock extends AirBlock {

    ZapAppleLeavesPlaceholderBlock() {
        super(Settings.create().replaceable().noCollision().dropsNothing().air());
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Deprecated
    @Override
    public void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);

        ZapAppleStageStore store = ZapAppleStageStore.get(world);
        ZapAppleStageStore.Stage newStage = store.getStage();
        if (!world.isDay() && ZapAppleStageStore.Stage.HIBERNATING.mustChangeIntoInstantly(newStage)) {
            state = newStage.getNewState(state);
            world.setBlockState(pos, state);
            BaseZapAppleLeavesBlock.onStageChanged(store, newStage, world, state, pos, random);
        }

        world.scheduleBlockTick(pos, state.getBlock(), 1);
    }
}
