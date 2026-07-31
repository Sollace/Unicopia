package com.minelittlepony.unicopia.block;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;

public interface CropBlockAccessor {
    float unicopia_getAvailableMoisture(BlockState state, BlockView world, BlockPos pos);
}
