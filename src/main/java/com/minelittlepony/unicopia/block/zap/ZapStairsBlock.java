package com.minelittlepony.unicopia.block.zap;

import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public class ZapStairsBlock extends StairsBlock {
    private final BlockState baseBlock;

    public ZapStairsBlock(BlockState baseBlockState, Settings settings) {
        super(baseBlockState, settings);
        this.baseBlock = baseBlockState;
    }

    @Deprecated
    @Override
    public float calcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos) {
        return baseBlock.calcBlockBreakingDelta(player, world, pos);
    }
}
