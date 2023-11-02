package com.minelittlepony.unicopia.block.cloud;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;

public class PoreousCloudStairsBlock extends CloudStairsBlock implements Soakable {

    protected final Supplier<Soakable> soggyBlock;

    public PoreousCloudStairsBlock(BlockState baseState, Settings settings, Supplier<Soakable> soggyBlock) {
        super(baseState, settings);
        this.soggyBlock = soggyBlock;
    }

    @Nullable
    @Override
    public BlockState getStateWithMoisture(BlockState state, int moisture) {
        if (moisture <= 0) {
            return Soakable.copyProperties(state, getDefaultState());
        }
        return soggyBlock.get().getStateWithMoisture(state, moisture);
    }
}
