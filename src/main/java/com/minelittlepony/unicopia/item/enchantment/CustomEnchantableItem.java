package com.minelittlepony.unicopia.item.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;

public interface CustomEnchantableItem {
    boolean isAcceptableEnchant(ItemStack stack, Enchantment enchantment);
}
