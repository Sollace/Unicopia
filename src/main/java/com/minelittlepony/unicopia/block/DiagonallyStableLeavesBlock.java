package com.minelittlepony.unicopia.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.LeavesBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.WorldAccess;

/**
 * Leaves block that checks the diagonals for whether they can decay.
 */
public class DiagonallyStableLeavesBlock extends LeavesBlock {
    private static final int MIN_DISTANCE = 1;

    public DiagonallyStableLeavesBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        world.setBlockState(pos, state.with(DISTANCE, getDistanceFromLogs(world, pos)), Block.NOTIFY_ALL);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState()
            .with(PERSISTENT, true)
            .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).isOf(Fluids.WATER))
            .with(DISTANCE, getDistanceFromLogs(ctx.getWorld(), ctx.getBlockPos()));
    }

    private static int getDistanceFromLogs(WorldAccess world, BlockPos pos) {
        int distance = MAX_DISTANCE;

        distance = Math.min(distance, getOptionalDistanceFromLog(world.getBlockState(pos.up())).orElse(MAX_DISTANCE) + 1);
        if (distance == MIN_DISTANCE) {
            return distance;
        }
        distance = Math.min(distance, getOptionalDistanceFromLog(world.getBlockState(pos.down())).orElse(MAX_DISTANCE) + 1);
        if (distance == MIN_DISTANCE) {
            return distance;
        }
        for (BlockPos i : BlockPos.iterate(pos.add(-1, 0, -1), pos.add(1, 0, 1))) {
            if (i.equals(pos)) {
                continue;
            }
            int increment = i.getX() == pos.getX() || i.getZ() == pos.getZ() ? 1 : 2;
            distance = Math.min(distance, getOptionalDistanceFromLog(world.getBlockState(i)).orElse(MAX_DISTANCE) + increment);
            if (distance == MIN_DISTANCE) {
                return distance;
            }
        }

        return distance;
    }
}
