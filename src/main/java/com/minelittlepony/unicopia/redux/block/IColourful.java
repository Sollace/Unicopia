package com.minelittlepony.unicopia.redux.block;

import net.minecraft.block.BlockState;

@FunctionalInterface
public interface IColourful {
    int getCustomTint(BlockState state, int tint);
}
