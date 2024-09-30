package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.item.ChameleonItem;
import com.minelittlepony.unicopia.item.EmptyJarItem;
import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.collection.DefaultedList;

public class JarInsertRecipe extends ItemCombinationRecipe {

    public JarInsertRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public final ItemStack craft(CraftingRecipeInput inventory, WrapperLookup registries) {
        return ChameleonItem.setAppearance(UItems.FILLED_JAR.getDefaultStack(), runMatch(inventory).getRight());
    }

    @Override
    protected boolean isContainerItem(ItemStack stack) {
        return stack.getItem() == UItems.EMPTY_JAR;
    }

    @Override
    protected boolean isInsertItem(ItemStack stack) {
        return !(stack.getItem() instanceof EmptyJarItem);
    }

    @Override
    protected boolean isCombinationInvalid(ItemStack bangle, ItemStack dust) {
        return false;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.JAR_INSERT_SERIALIZER;
    }

    @Override
    public DefaultedList<ItemStack> getRemainder(CraftingRecipeInput inventory) {
        return DefaultedList.ofSize(inventory.getSize(), ItemStack.EMPTY);
    }
}
