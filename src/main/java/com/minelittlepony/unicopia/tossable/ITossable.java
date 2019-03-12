package com.minelittlepony.unicopia.tossable;

import com.minelittlepony.unicopia.spell.ICaster;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public interface ITossable<T> {

    /**
     * Called once the projectile lands either hitting the ground or an entity.
     */
    void onImpact(ICaster<?> caster, BlockPos pos, IBlockState state);

    /**
     * The sound made when thrown.
     */
    default SoundEvent getThrowSound(T stack) {
        return SoundEvents.ENTITY_SNOWBALL_THROW;
    }

    /**
     * The amount of damage to be dealt when the projectile collides with an entity.
     */
    default int getThrowDamage(T stack) {
        return 0;
    }
}
