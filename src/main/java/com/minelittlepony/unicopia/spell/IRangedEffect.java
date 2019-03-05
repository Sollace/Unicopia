package com.minelittlepony.unicopia.spell;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IRangedEffect extends IMagicEffect {
    void onImpact(World world, BlockPos pos, IBlockState state);

    default SoundEvent getThrowSound(ItemStack stack) {
        return SoundEvents.ENTITY_SNOWBALL_THROW;
    }
}
