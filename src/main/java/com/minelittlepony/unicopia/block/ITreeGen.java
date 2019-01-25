package com.minelittlepony.unicopia.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

@FunctionalInterface
public interface ITreeGen {
    WorldGenAbstractTree getTreeGen(World world, IBlockState state, boolean massive);

    default boolean canGrowMassive() {
        return false;
    }
}