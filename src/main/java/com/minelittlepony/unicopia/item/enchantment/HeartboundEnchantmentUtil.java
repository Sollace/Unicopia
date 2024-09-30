package com.minelittlepony.unicopia.item.enchantment;

import java.util.List;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface HeartboundEnchantmentUtil {
    static InventorySnapshot createSnapshot(List<DefaultedList<ItemStack>> combinedInventory) {
        List<DefaultedList<ItemStack>> storedCombinedInventory = combinedInventory.stream().map(l -> DefaultedList.ofSize(l.size(), ItemStack.EMPTY)).toList();
        boolean empty = true;
        for (int group = 0; group < combinedInventory.size(); group++) {
            var original = combinedInventory.get(group);
            for (int i = 0; i < original.size(); i++) {
                ItemStack stack = original.get(i);
                if (EnchantmentUtil.getLevel(Enchantments.BINDING_CURSE, stack) == 0
                    && EnchantmentUtil.getLevel(UEnchantments.HEART_BOUND, stack) > 0) {
                    original.set(i, ItemStack.EMPTY);
                    storedCombinedInventory.get(group).set(i, stack);
                    empty = false;
                }
            }
        }
        return empty ? InventorySnapshot.EMPTY : new InventorySnapshot(storedCombinedInventory);
    }

    public record InventorySnapshot(List<DefaultedList<ItemStack>> combinedInventory) {
        public static InventorySnapshot EMPTY = new InventorySnapshot(List.of());

        public boolean empty() {
            return combinedInventory.isEmpty();
        }

        public void restoreInto(List<DefaultedList<ItemStack>> combinedInventory) {
            if (empty()) {
                return;
            }
            for (int group = 0; group < combinedInventory.size(); group++) {
                var original = combinedInventory.get(group);
                for (int i = 0; i < original.size(); i++) {
                    ItemStack stored = this.combinedInventory.get(group).get(i);
                    if (!stored.isEmpty()) {
                        original.set(i, stored);
                    }
                }
            }
        }
    }
}
