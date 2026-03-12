package com.minelittlepony.unicopia.datagen.providers.recipe;

import com.minelittlepony.unicopia.recipe.ItemConversionShapedRecipe;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;

public class ItemConversionShapedRecipeBuilder extends CustomShapedRecipeBuilder<ItemConversionShapedRecipe, ItemConversionShapedRecipeBuilder> {
    private final Item base;

    private ItemConversionShapedRecipeBuilder(RecipeCategory category, ItemConvertible base, ItemConvertible output, int count) {
        super(category, output, count);
        this.base = base.asItem();
    }

    public static ItemConversionShapedRecipeBuilder create(RecipeCategory category, ItemConvertible base, ItemConvertible output) {
        return create(category, base, output, 1);
    }

    public static ItemConversionShapedRecipeBuilder create(RecipeCategory category, ItemConvertible base, ItemConvertible output, int count) {
        return new ItemConversionShapedRecipeBuilder(category, base, output, count);
    }

    @Override
    protected ItemConversionShapedRecipe createRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe shape, ItemStack output, boolean showNotification) {
        return new ItemConversionShapedRecipe(group, category, shape, base.getDefaultStack(), output, showNotification);
    }
}