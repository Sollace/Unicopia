package com.minelittlepony.unicopia.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FrostedIceBlock;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FrostedObsidianBlock extends FrostedIceBlock {
    public FrostedObsidianBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void melt(BlockState state, World world, BlockPos pos) {
        if (world.getDimension().isUltrawarm()) {
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
