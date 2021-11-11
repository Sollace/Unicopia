package com.minelittlepony.unicopia.util;

import com.google.common.collect.AbstractIterator;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface InventoryUtil {
    static Iterable<ItemStack> iterate(Inventory inventory) {
        return () -> new AbstractIterator<>() {
            private int slot = 0;

            @Override
            protected ItemStack computeNext() {
                if (slot >= inventory.size()) {
                    return endOfData();
                }
                return inventory.getStack(slot++);
            }
        };
    }
}
