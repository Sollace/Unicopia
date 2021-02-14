package com.minelittlepony.unicopia.item;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public class JarInsertRecipe extends ItemCombinationRecipe {

    public JarInsertRecipe(Identifier id) {
        super(id);
    }

    @Override
    public final ItemStack craft(CraftingInventory inventory) {
        Pair<ItemStack, ItemStack> pair = runMatch(inventory);

        return UItems.FILLED_JAR.setAppearance(UItems.FILLED_JAR.getDefaultStack(), pair.getRight());
    }

    @Override
    protected boolean isContainerItem(ItemStack stack) {
        return stack.getItem() == UItems.EMPTY_JAR;
    }

    @Override
    protected boolean isInsertItem(ItemStack stack) {
        return !(stack.getItem() instanceof JarItem);
    }

    @Override
    protected boolean isCombinationInvalid(ItemStack bangle, ItemStack dust) {
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.JAR_INSERT_SERIALIZER;
    }
}
