package com.minelittlepony.unicopia.block;

import com.minelittlepony.unicopia.server.world.ZapAppleStageStore;
import com.mojang.serialization.MapCodec;

import net.minecraft.block.*;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.get(PERSISTENT)
                || oldState.isOf(state.getBlock())
                || oldState.isOf(UBlocks.ZAP_LEAVES)
                || oldState.isOf(UBlocks.FLOWERING_ZAP_LEAVES)
                || oldState.isOf(UBlocks.ZAP_LEAVES_PLACEHOLDER)
                || !(world instanceof ServerWorld sw)) {
            return;
        }

        ZapAppleStageStore store = ZapAppleStageStore.get(sw);
        ZapAppleStageStore.Stage currentStage = store.getStage();
        if (currentStage != getStage(state)) {
            world.setBlockState(pos, currentStage.getNewState(state));
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(STAGE, ZapAppleStageStore.Stage.GREENING);
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
