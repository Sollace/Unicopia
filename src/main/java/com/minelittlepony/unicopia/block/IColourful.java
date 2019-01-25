package com.minelittlepony.unicopia.block;

import net.minecraft.block.state.IBlockState;

public interface IColourful {
    int getCustomTint(IBlockState state, int tint);
}
