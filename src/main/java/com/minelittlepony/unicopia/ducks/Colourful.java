package com.minelittlepony.unicopia.ducks;

import net.minecraft.block.BlockState;

@FunctionalInterface
public interface Colourful {
    int getCustomTint(BlockState state, int tint);
}
