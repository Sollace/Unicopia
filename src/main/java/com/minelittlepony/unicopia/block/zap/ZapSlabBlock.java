package com.minelittlepony.unicopia.block.zap;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ZapSlabBlock extends SlabBlock implements ElectrifiedBlock {
    private static final MapCodec<ZapSlabBlock> CODEC = createCodec(ZapSlabBlock::new);

    public ZapSlabBlock(Settings settings) {
        super(settings);
    }

    @Override
    public MapCodec<? extends SlabBlock> getCodec() {
        return CODEC;
    }

    @Deprecated
    @Override
    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        triggerLightning(state, world, pos);
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        return getBlockBreakingDelta(super.calcBlockBreakingDelta(state, player, world, pos), player);
    }
}
