package com.minelittlepony.unicopia.item.enchantment;

import java.util.List;

import com.minelittlepony.unicopia.util.InventoryUtil;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

public interface HeartboundEnchantmentUtil {
    static InventorySnapshot createSnapshot(Inventory inventory) {
        return inventory.isEmpty() ? InventorySnapshot.EMPTY : new InventorySnapshot(InventoryUtil.stream(inventory).toList());
    }

    public record InventorySnapshot(List<ItemStack> stacks, boolean empty) {
        public static final InventorySnapshot EMPTY = new InventorySnapshot(List.of(), true);

        public InventorySnapshot(List<ItemStack> stacks) {
            this(stacks, stacks.stream().anyMatch(i -> !i.isEmpty()));
        }

        public void restoreInto(Inventory inventory) {
            for (int i = 0; i < inventory.size(); i++) {
                inventory.setStack(i, i >= stacks.size() ? ItemStack.EMPTY : stacks.get(i));
            }
        }
    }
}
