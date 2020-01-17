package com.minelittlepony.unicopia.item.consumables;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface IEdible {
    Toxicity getToxicityLevel(ItemStack stack);

    @Nonnull
    default void addSecondaryEffects(PlayerEntity player, Toxicity toxicity, ItemStack stack) {

    }
}
