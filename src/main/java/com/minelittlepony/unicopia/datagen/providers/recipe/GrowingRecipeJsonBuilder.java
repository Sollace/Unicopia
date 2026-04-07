package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.recipe.TransformCropsRecipe;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class GrowingRecipeJsonBuilder {
    private final Map<String, AdvancementCriterion<?>> criterions = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final RecipeCategory category;
    private final BlockState output;
    private Block target;
    private BlockState fuel;

    public static GrowingRecipeJsonBuilder create(RecipeCategory category, BlockState output) {
        return new GrowingRecipeJsonBuilder(category, output);
    }

    protected GrowingRecipeJsonBuilder(RecipeCategory category, BlockState output) {
        this.category = category;
        this.output = output;
    }

    public GrowingRecipeJsonBuilder target(Block target) {
        this.target = target;
        return this;
    }

    public GrowingRecipeJsonBuilder fuel(BlockState fuel) {
        this.fuel = fuel;
        return this;
    }

    public GrowingRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> condition) {
        criterions.put(name, condition);
        return this;
    }

    public GrowingRecipeJsonBuilder group(String group) {
        this.group = group;
        return this;
    }

    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> key) {
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + key.getValue());
        Advancement.Builder advancementBuilder = exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(key))
                .rewards(AdvancementRewards.Builder.recipe(key))
                .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        criterions.forEach(advancementBuilder::criterion);
        exporter.accept(key, new TransformCropsRecipe(target, fuel, output), advancementBuilder.build(key.getValue().withPrefixedPath("recipes/" + category.getName() + "/")));
    }

    public void offerTo(RecipeExporter exporter) {
        offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, Registries.BLOCK.getId(output.getBlock())));
    }

    public void offerTo(RecipeExporter exporter, String recipePath) {
        Identifier recipeId = Identifier.of(recipePath);
        Identifier id = Registries.BLOCK.getId(output.getBlock());
        if (recipeId.equals(id)) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE, recipeId));
    }
}
