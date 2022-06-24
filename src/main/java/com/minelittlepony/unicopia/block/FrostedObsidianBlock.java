package com.minelittlepony.unicopia.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FrostedIceBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FrostedObsidianBlock extends FrostedIceBlock {
    public FrostedObsidianBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack stack) {
        player.incrementStat(Stats.MINED.getOrCreateStat(this));
        player.addExhaustion(0.005f);
        Block.dropStacks(state, world, pos, blockEntity, player, stack);
    }

    @Override
    protected void melt(BlockState state, World world, BlockPos pos) {
        if (world.getDimension().ultrawarm()) {
            world.removeBlock(pos, false);
            return;
        }
        world.setBlockState(pos, Blocks.LAVA.getDefaultState());
        world.updateNeighbor(pos, Blocks.LAVA, pos);
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.BLOCK;
    }
}
