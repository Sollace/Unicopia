package com.minelittlepony.unicopia.tossable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ITossable<T> {
    void onImpact(World world, BlockPos pos, IBlockState state);

    default SoundEvent getThrowSound(T stack) {
        return SoundEvents.ENTITY_SNOWBALL_THROW;
    }

    default int getThrowDamage(T stack) {
        return 0;
    }
}
