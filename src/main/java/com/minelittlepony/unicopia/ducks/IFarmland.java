package com.minelittlepony.unicopia.ducks;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IFarmland {
    /**
     * Gets the state used to represent this block as a piece of dirt.
     */
    BlockState getDirtState(BlockState state, World world, BlockPos pos);
}
