package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;

public class ZapAppleLeavesBlock extends BaseZapAppleLeavesBlock {
    public static final MapCodec<ZapAppleLeavesBlock> CODEC = createCodec(ZapAppleLeavesBlock::new);
    public static final EnumProperty<ZapAppleStageStore.Stage> STAGE = EnumProperty.of("stage", ZapAppleStageStore.Stage.class);

    ZapAppleLeavesBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(STAGE, ZapAppleStageStore.Stage.GREENING));
    }

    @Override
    public MapCodec<? extends ZapAppleLeavesBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(STAGE, ZapAppleStageStore.Stage.GREENING);
    }

    @Override
    public ZapAppleStageStore.Stage getStage(BlockState state) {
        return state.get(STAGE);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(STAGE);
    }
}
