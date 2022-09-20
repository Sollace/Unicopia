package com.minelittlepony.unicopia.util;

import java.util.stream.Stream;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface InventoryUtil {
    static Stream<ItemStack> stream(Inventory inventory) {
        return slots(inventory).map(inventory::getStack);
    }

    static Stream<Integer> slots(Inventory inventory) {
        return Stream.iterate(0, i -> i < inventory.size(), i -> i + 1);
    }

    static int getOpenSlot(Inventory inventory) {
        for (int i = 0; i < inventory.size(); i++) {
            if (inventory.getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}
