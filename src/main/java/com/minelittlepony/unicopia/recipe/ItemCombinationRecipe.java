package com.minelittlepony.unicopia.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.util.Pair;
import net.minecraft.world.World;

public abstract class ItemCombinationRecipe extends SpecialCraftingRecipe {

    public ItemCombinationRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public final boolean fits(int i, int j) {
        return i * j >= 2;
    }

    @Override
    public final boolean matches(CraftingRecipeInput inventory, World world) {
        Pair<ItemStack, ItemStack> result = runMatch(inventory);

        return !result.getLeft().isEmpty() && !result.getRight().isEmpty();
    }

    protected Pair<ItemStack, ItemStack> runMatch(CraftingRecipeInput inventory) {
        ItemStack bangle = ItemStack.EMPTY;
        ItemStack dust = ItemStack.EMPTY;

        for(int i = 0; i < inventory.getSize(); i++) {
           ItemStack stack = inventory.getStackInSlot(i);

           if (!stack.isEmpty()) {
              if (isContainerItem(stack)) {
                 if (!bangle.isEmpty()) {
                     return new Pair<>(bangle, dust);
                 }

                 bangle = stack;
              } else {
                 if (!dust.isEmpty() || !isInsertItem(stack)) {
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
