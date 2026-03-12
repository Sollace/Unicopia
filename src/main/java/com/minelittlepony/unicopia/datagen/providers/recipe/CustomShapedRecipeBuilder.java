package com.minelittlepony.unicopia.datagen.providers.recipe;

import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.util.Untyped;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public abstract class CustomShapedRecipeBuilder<T extends ShapedRecipe, B extends CustomShapedRecipeBuilder<T, B>> implements CraftingRecipeJsonBuilder {
    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final List<String> pattern = new ArrayList<>();
    private final Map<Character, Ingredient> inputs = new LinkedHashMap<>();
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;
    private boolean showNotification = true;

    public CustomShapedRecipeBuilder(RecipeCategory category, ItemConvertible output, int count) {
        this.category = category;
        this.output = output.asItem();
        this.count = count;
    }

    public B input(Character c, TagKey<Item> tag) {
        return input(c, Ingredient.fromTag(tag));
    }

    public B input(Character c, ItemConvertible itemProvider) {
        return input(c, Ingredient.ofItems(itemProvider));
    }

    public B input(Character c, Ingredient ingredient) {
        Preconditions.checkArgument(!inputs.containsKey(c), "Symbol '" + c + "' is already defined!");
        Preconditions.checkArgument(c != ' ', "Symbol ' ' (whitespace) is reserved and cannot be defined");
        inputs.put(c, ingredient);
        return Untyped.cast(this);
    }

    public B pattern(String patternStr) {
        Preconditions.checkArgument(pattern.isEmpty() || patternStr.length() == pattern.get(0).length(), "Pattern must be the same width on every line!");
        pattern.add(patternStr);
        return Untyped.cast(this);
    }

    @Override
    public B criterion(String key, AdvancementCriterion<?> criterion) {
        criteria.put(key, criterion);
        return Untyped.cast(this);
    }

    @Override
    public B group(@Nullable String group) {
        this.group = group;
        return Untyped.cast(this);
    }

    public B showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return Untyped.cast(this);
    }

    @Override
    public Item getOutputItem() {
        return output;
    }

    @Override
    public void offerTo(RecipeExporter exporter, Identifier recipeId) {
        Preconditions.checkState(!criteria.isEmpty(), "No way of obtaining recipe " + recipeId);
        Advancement.Builder builder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(recipeId))
            .rewards(AdvancementRewards.Builder.recipe(recipeId))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        criteria.forEach(builder::criterion);
        exporter.accept(recipeId, createRecipe(
                Objects.requireNonNullElse(group, ""),
                CraftingRecipeJsonBuilder.toCraftingCategory(category),
                RawShapedRecipe.create(inputs, pattern),
                new ItemStack(output, count),
                showNotification
            ), builder.build(recipeId.withPrefixedPath("recipes/" + category.getName() + "/")));
    }

    protected abstract T createRecipe(String group, CraftingRecipeCategory category, RawShapedRecipe shape, ItemStack output, boolean showNotification);
}