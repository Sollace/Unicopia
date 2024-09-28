package com.minelittlepony.unicopia.item.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;

@Deprecated
public interface CustomEnchantableItem {
    boolean isAcceptableEnchant(ItemStack stack, RegistryKey<Enchantment> enchantment);
}
