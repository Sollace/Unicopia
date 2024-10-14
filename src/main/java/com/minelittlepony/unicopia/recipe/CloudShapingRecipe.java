package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.util.Untyped;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.StonecuttingRecipe;

public class CloudShapingRecipe extends StonecuttingRecipe {
    public CloudShapingRecipe(String group, Ingredient input, ItemStack output) {
        super(group, input, output);
    }

    @Override
    public boolean isIgnoredInRecipeBook() {
        return true;
    }

    @Override
    public RecipeType<StonecuttingRecipe> getType() {
        return Untyped.cast(URecipes.CLOUD_SHAPING);
    }

    @Override
    public RecipeSerializer<StonecuttingRecipe> getSerializer() {
        return Untyped.cast(URecipes.CLOUD_SHAPING_SERIALIZER);
    }

    @Override
    public ItemStack createIcon() {
        return new ItemStack(UBlocks.SHAPING_BENCH);
    }
}
