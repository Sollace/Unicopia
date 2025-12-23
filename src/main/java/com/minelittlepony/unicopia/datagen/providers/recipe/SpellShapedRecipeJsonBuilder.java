package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.common.collect.Lists;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellShapedCraftingRecipe;

import net.minecraft.advancement.Advancement.Builder;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

public class SpellShapedRecipeJsonBuilder extends ShapedRecipeJsonBuilder {
    private final List<String> pattern = Lists.<String>newArrayList();
    private final Map<Character, Ingredient> inputs = new LinkedHashMap<>();

    public SpellShapedRecipeJsonBuilder(RecipeCategory category, ItemConvertible output, int count) {
        super(category, output, count);
    }

    public static SpellShapedRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output) {
        return create(category, output, 1);
    }

    public static SpellShapedRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output, int count) {
        return new SpellShapedRecipeJsonBuilder(category, output, count);
    }

    @Override
    public ShapedRecipeJsonBuilder input(Character c, Ingredient ingredient) {
        Preconditions.checkArgument(!inputs.containsKey(c), "Symbol '" + c + "' is already defined!");
        Preconditions.checkArgument(c != ' ', "Symbol ' ' (whitespace) is reserved and cannot be defined");
        inputs.put(c, ingredient);
        return this;
    }

    @Override
    public ShapedRecipeJsonBuilder pattern(String patternStr) {
        Preconditions.checkArgument(pattern.isEmpty() || patternStr.length() == pattern.get(0).length(), "Pattern must be the same width on every line!");
        pattern.add(patternStr);
        return this;
    }

    @Override
    public void offerTo(RecipeExporter exporter, Identifier recipeId) {
        super.offerTo(new RecipeExporter() {
            @Override
            public void accept(Identifier recipeId, Recipe<?> recipe, AdvancementEntry advancement) {
                exporter.accept(recipeId, new SpellShapedCraftingRecipe(
                        recipe.getGroup(),
                        ((ShapedRecipe)recipe).getCategory(),
                        RawShapedRecipe.create(inputs, pattern),
                        ((ShapedRecipe)recipe).getResult(null),
                        recipe.showNotification()), advancement);
            }

            @Override
            public Builder getAdvancementBuilder() {
                return exporter.getAdvancementBuilder();
            }

        }, recipeId);
    }
}
