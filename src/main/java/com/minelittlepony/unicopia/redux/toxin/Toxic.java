package com.minelittlepony.unicopia.redux.toxin;

import net.minecraft.item.ItemStack;

@FunctionalInterface
public interface Toxic {
    Toxicity getToxicity(ItemStack stack);
}
