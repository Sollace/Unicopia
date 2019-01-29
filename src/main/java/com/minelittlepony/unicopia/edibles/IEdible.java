package com.minelittlepony.unicopia.edibles;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface IEdible {
    Toxicity getToxicityLevel(ItemStack stack);

    @Nonnull
    default void addSecondaryEffects(EntityPlayer player, Toxicity toxicity, ItemStack stack) {

    }
}
