package com.minelittlepony.unicopia.block;

import net.minecraft.block.state.IBlockState;

@FunctionalInterface
public interface IColourful {
    int getCustomTint(IBlockState state, int tint);
}
