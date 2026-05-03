package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellShapedCraftingRecipe;

import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;

public class SpellShapedRecipeJsonBuilder extends CustomShapedRecipeBuilder<SpellShapedCraftingRecipe, SpellShapedRecipeJsonBuilder> {
    @Nullable
    private Ingredient spellCopySource;

    private SpellShapedRecipeJsonBuilder(RecipeCategory category, ItemConvertible output, int count) {
        super(category, output, count);
    }

    public static SpellShapedRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output) {
        return create(category, output, 1);
    }

    public static SpellShapedRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, int count) {
        return new SpellShapedRecipeJsonBuilder(category, output, count);
    }

    public SpellShapedRecipeJsonBuilder withSpellFrom(ItemConvertible item) {
        return withSpellFrom(Ingredient.ofItems(item));
    }

    public SpellShapedRecipeJsonBuilder withSpellFrom(Ingredient ingredient) {
        spellCopySource = ingredient;
        return this;
    }

    @Override
    protected SpellShapedCraftingRecipe createRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe shape, ItemStack output, boolean showNotification) {
        return new SpellShapedCraftingRecipe(group, category, shape, Optional.ofNullable(spellCopySource), output, showNotification);
    }
}
