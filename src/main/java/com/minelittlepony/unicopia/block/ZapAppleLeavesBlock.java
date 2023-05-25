package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;

import net.minecraft.block.*;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;

public class ZapAppleLeavesBlock extends BaseZapAppleLeavesBlock {
    public static final EnumProperty<ZapAppleStageStore.Stage> STAGE = EnumProperty.of("stage", ZapAppleStageStore.Stage.class);

    ZapAppleLeavesBlock() {
        setDefaultState(getDefaultState().with(STAGE, ZapAppleStageStore.Stage.HIBERNATING));
    }

    @Override
    protected ZapAppleStageStore.Stage getStage(BlockState state) {
        return state.get(STAGE);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(STAGE);
    }
}
