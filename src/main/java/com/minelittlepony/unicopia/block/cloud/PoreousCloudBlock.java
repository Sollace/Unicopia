package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

public class PoreousCloudBlock extends CloudBlock implements Soakable {
    @Nullable
    private final Supplier<Soakable> soggyBlock;

    public PoreousCloudBlock(Settings settings, boolean meltable, @Nullable Supplier<Soakable> soggyBlock) {
        super(settings.nonOpaque(), meltable);
        this.soggyBlock = soggyBlock;
    }

    @Nullable
    @Override
    public BlockState getSoggyState(int moisture) {
        return soggyBlock == null ? null : soggyBlock.get().getSoggyState(moisture);
    }

    @Override
    public int getMoisture(BlockState state) {
        return 0;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (soggyBlock != null && world.hasRain(pos) && world.isAir(pos.up())) {
            world.setBlockState(pos, Soakable.copyProperties(state, soggyBlock.get().getSoggyState(random.nextBetween(1, 5))));
            return;
        }

        super.randomTick(state, world, pos, random);
    }
}
