package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellShapedCraftingRecipe;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
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
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;

public class SpellShapedRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final RegistryEntryLookup<Item> items;
    private final RecipeCategory category;
    private final Item output;
    private final int count;
    private final List<String> pattern = Lists.<String>newArrayList();
    private final Map<Character, Ingredient> inputs = new LinkedHashMap<>();
    private final Map<String, AdvancementCriterion<?>> criteria = new LinkedHashMap<>();

    @Nullable
    private String group;
    private boolean showNotification = true;

    public SpellShapedRecipeJsonBuilder(RegistryEntryLookup<Item> items, RecipeCategory category, ItemConvertible output, int count) {
        this.category = category;
        this.items = items;
        this.output = output.asItem();
        this.count = count;
    }

    public static SpellShapedRecipeJsonBuilder create(RegistryEntryLookup<Item> items, RecipeCategory category, ItemConvertible output) {
        return create(items, category, output, 1);
    }

    public static SpellShapedRecipeJsonBuilder create(RegistryEntryLookup<Item> items, RecipeCategory category, ItemConvertible output, int count) {
        return new SpellShapedRecipeJsonBuilder(items, category, output, count);
    }

    public SpellShapedRecipeJsonBuilder input(Character c, TagKey<Item> tag) {
        return input(c, Ingredient.fromTag(items.getOrThrow(tag)));
    }

    public SpellShapedRecipeJsonBuilder input(Character c, ItemConvertible item) {
        return input(c, Ingredient.ofItem(item));
    }

    public SpellShapedRecipeJsonBuilder input(Character c, Ingredient ingredient) {
        Preconditions.checkArgument(!inputs.containsKey(c), "Symbol '" + c + "' is already defined!");
        Preconditions.checkArgument(c != ' ', "Symbol ' ' (whitespace) is reserved and cannot be defined");
        inputs.put(c, ingredient);
        return this;
    }

    public SpellShapedRecipeJsonBuilder pattern(String patternStr) {
        Preconditions.checkArgument(pattern.isEmpty() || patternStr.length() == pattern.get(0).length(), "Pattern must be the same width on every line!");
        pattern.add(patternStr);
        return this;
    }

    @Override
    public SpellShapedRecipeJsonBuilder criterion(String string, AdvancementCriterion<?> advancementCriterion) {
        criteria.put(string, advancementCriterion);
        return this;
    }

    @Override
    public SpellShapedRecipeJsonBuilder group(@Nullable String string) {
        this.group = string;
        return this;
    }

    public SpellShapedRecipeJsonBuilder showNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    @Override
    public Item getOutputItem() {
        return output;
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> key) {
        Preconditions.checkArgument(!criteria.isEmpty(), "No way of obtaining recipe " + key.getValue());
        Advancement.Builder builder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(key))
            .rewards(AdvancementRewards.Builder.recipe(key))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        criteria.forEach(builder::criterion);
        SpellShapedCraftingRecipe shapedRecipe = new SpellShapedCraftingRecipe(
            Objects.requireNonNullElse(group, ""),
            CraftingRecipeJsonBuilder.toCraftingCategory(category),
            RawShapedRecipe.create(inputs, pattern),
            new ItemStack(output, count),
            showNotification
        );
        exporter.accept(key, shapedRecipe, builder.build(key.getValue().withPrefixedPath("recipes/" + category.getName() + "/")));
    }

}
