package com.minelittlepony.unicopia.block;

import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.light.ChunkLightProvider;

public class GrowableBlock extends SpreadableBlock {

    private final Supplier<Block> dead;

    protected GrowableBlock(Settings settings, Supplier<Block> converted) {
        super(settings);
        this.dead = converted;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!canSurvive(state, world, pos)) {
            world.setBlockState(pos, dead.get().getDefaultState());
            return;
        }

        if (world.getLightLevel(pos.up()) >= 9) {
            BlockState blockState = getDefaultState();
            for (int i = 0; i < 4; i++) {
                BlockPos blockPos = pos.add(
                        random.nextInt(3) - 1,
                        random.nextInt(5) - 3,
                        random.nextInt(3) - 1
                );

                if (canSpread(blockState, world, blockPos)) {
                    world.setBlockState(blockPos, blockState.with(SNOWY, world.getBlockState(blockPos.up()).isOf(Blocks.SNOW)));
                }
            }
        }
    }

    private boolean canSpread(BlockState state, WorldView world, BlockPos pos) {
        return world.getBlockState(pos).isOf(dead.get())
                && !world.getFluidState(pos.up()).isIn(FluidTags.WATER)
                && canSurvive(state, world, pos);
    }

    private boolean canSurvive(BlockState state, WorldView world, BlockPos pos) {
        BlockPos above = pos.up();
        BlockState stateAbove = world.getBlockState(above);
        if (stateAbove.isOf(Blocks.SNOW) && stateAbove.get(SnowBlock.LAYERS) == 1) {
            return true;
        }
        if (stateAbove.getFluidState().getLevel() == 8) {
            return false;
        }

        return ChunkLightProvider.getRealisticOpacity(world, state, pos, stateAbove, above, Direction.UP, stateAbove.getOpacity(world, above)) < world.getMaxLightLevel();
    }
}
