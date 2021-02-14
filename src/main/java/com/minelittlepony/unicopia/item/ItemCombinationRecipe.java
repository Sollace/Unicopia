package com.minelittlepony.unicopia.item;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public abstract class ItemCombinationRecipe extends SpecialCraftingRecipe {

    public ItemCombinationRecipe(Identifier id) {
        super(id);
    }

    @Override
    public final boolean fits(int i, int j) {
        return i * j >= 2;
    }

    @Override
    public final boolean matches(CraftingInventory inventory, World world) {
        Pair<ItemStack, ItemStack> result = runMatch(inventory);

        return !result.getLeft().isEmpty() && !result.getRight().isEmpty();
    }

    protected Pair<ItemStack, ItemStack> runMatch(CraftingInventory inventory) {
        ItemStack bangle = ItemStack.EMPTY;
        ItemStack dust = ItemStack.EMPTY;

        for(int i = 0; i < inventory.size(); i++) {
           ItemStack stack = inventory.getStack(i);

           if (!stack.isEmpty()) {
              if (isContainerItem(stack)) {
                 if (!bangle.isEmpty()) {
                     return new Pair<>(bangle, dust);
                 }

                 bangle = stack;
              } else {
                 if (!isInsertItem(stack)) {
                     return new Pair<>(bangle, dust);
                 }

                 dust = stack;
              }
           }
        }

        if (!bangle.isEmpty() && isCombinationInvalid(bangle, dust)) {
            return new Pair<>(ItemStack.EMPTY, ItemStack.EMPTY);
        }

        return new Pair<>(bangle, dust);
    }

    protected boolean isCombinationInvalid(ItemStack bangle, ItemStack dust) {
        return false;
    }

    protected abstract boolean isContainerItem(ItemStack stack);

    protected abstract boolean isInsertItem(ItemStack stack);
}
