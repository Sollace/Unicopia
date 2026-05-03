package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellShapedCraftingRecipe;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;

public class SpellShapedRecipeJsonBuilder extends CustomShapedRecipeBuilder<SpellShapedCraftingRecipe, SpellShapedRecipeJsonBuilder> {
    @Nullable
    private Ingredient spellCopySource;

    private SpellShapedRecipeJsonBuilder(RegistryEntryLookup<Item> items, RecipeCategory category, ItemConvertible output, int count) {
        super(items, category, output, count);
    }

    public static SpellShapedRecipeJsonBuilder create(RegistryEntryLookup<Item> items, RecipeCategory category, ItemConvertible output) {
        return create(items, category, output, 1);
    }

    public static SpellShapedRecipeJsonBuilder create(RegistryEntryLookup<Item> items, RecipeCategory category, ItemConvertible output, int count) {
        return new SpellShapedRecipeJsonBuilder(items, category, output, count);
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
