package com.minelittlepony.unicopia.util;

import java.util.function.Function;
import java.util.stream.Stream;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.input.RecipeInput;

public interface InventoryUtil {
    static Stream<ItemStack> stream(Inventory inventory) {
        return slots(inventory).map(inventory::getStack);
    }

    static Stream<ItemStack> stream(RecipeInput inventory) {
        return slots(inventory).map(inventory::getStackInSlot);
    }

    static Stream<Integer> slots(Inventory inventory) {
        return Stream.iterate(0, i -> i < inventory.size(), i -> i + 1);
    }

    static Stream<Integer> slots(RecipeInput inventory) {
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

    static boolean contentEquals(Inventory a, Inventory b) {
        if (a.size() != b.size()) {
            return false;
        }
        for (int i = 0; i < a.size(); i++) {
            if (!ItemStack.areEqual(a.getStack(i), b.getStack(i))) {
                return false;
            }
        }
        return true;
    }

    static <I extends Inventory> I copy(Inventory from, Function<Integer, I> factory) {
        return copyInto(from, factory.apply(from.size()));
    }

    static <I extends Inventory> I copyInto(Inventory from, I into) {
        for (int i = 0; i < from.size(); i++) {
            into.setStack(i, from.getStack(i).copy());
        }
        return into;
    }
}
