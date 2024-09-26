package com.minelittlepony.unicopia.recipe;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.world.World;

public class JarExtractRecipe extends SpecialCraftingRecipe {
    public JarExtractRecipe(CraftingRecipeCategory category) {
        super(category);
    }

    @Override
    public final boolean fits(int i, int j) {
        return i * j >= 1;
    }

    @Override
    public final boolean matches(CraftingRecipeInput inventory, World world) {
        return !craft(inventory, null).isEmpty();
    }

    @Override
    public ItemStack craft(CraftingRecipeInput inventory, @Nullable WrapperLookup manager) {
        ItemStack jar = ItemStack.EMPTY;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (!stack.isOf(UItems.FILLED_JAR)) {
                return ItemStack.EMPTY;
            }

            if (!jar.isEmpty()) {
                return ItemStack.EMPTY;
            }

            if (!UItems.FILLED_JAR.hasAppearance(stack)) {
                return ItemStack.EMPTY;
            }

            jar = stack;
        }

        return UItems.FILLED_JAR.getAppearanceStack(jar);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return URecipes.JAR_INSERT_SERIALIZER;
    }
}