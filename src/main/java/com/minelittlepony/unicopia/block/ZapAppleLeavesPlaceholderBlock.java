package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;

import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

public class ZapAppleLeavesPlaceholderBlock extends AirBlock {

    ZapAppleLeavesPlaceholderBlock() {
        super(Settings.create().replaceable().noCollision().dropsNothing().air());
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {

        if (world instanceof ServerWorld sw) {
            ZapAppleStageStore store = ZapAppleStageStore.get(sw);
            ZapAppleStageStore.Stage currentStage = store.getStage();
            if (currentStage != ZapAppleStageStore.Stage.HIBERNATING) {
                return currentStage.getNewState(state);
            }
        }

        return state;
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
