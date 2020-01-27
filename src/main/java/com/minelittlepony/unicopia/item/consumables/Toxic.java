package com.minelittlepony.unicopia.item.consumables;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface Toxic {
    Toxicity getToxicity(ItemStack stack);
}
