package com.minelittlepony.unicopia.block;

import net.minecraft.block.BlockState;
import net.minecraft.world.World;

@FunctionalInterface
public interface ITreeGen {
    WorldGenAbstractTree getTreeGen(World world, BlockState state, boolean massive);

    default boolean canGrowMassive() {
        return false;
    }
}