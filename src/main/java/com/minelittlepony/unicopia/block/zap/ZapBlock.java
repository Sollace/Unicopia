package com.minelittlepony.unicopia.block.zap;

import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class ZapBlock extends Block implements ElectrifiedBlock {
    public ZapBlock(Settings settings) {
        super(settings);
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
