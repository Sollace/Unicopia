package com.minelittlepony.unicopia.toxin;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface Toxic {
    Toxicity getToxicity(ItemStack stack);
}
