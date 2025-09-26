package com.minelittlepony.unicopia.block;

import net.minecraft.item.ItemUsageContext;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;

public interface StrippingLootable {
    boolean onStripped(ItemUsageContext context);

    RegistryKey<LootTable> getStrippingLootTableKey();
}
