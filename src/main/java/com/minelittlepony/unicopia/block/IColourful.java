package com.minelittlepony.unicopia.block;

import net.minecraft.block.BlockState;

@FunctionalInterface
public interface IColourful {
    int getCustomTint(BlockState state, int tint);
}
