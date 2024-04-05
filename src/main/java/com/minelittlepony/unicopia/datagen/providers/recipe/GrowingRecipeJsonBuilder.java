package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.recipe.TransformCropsRecipe;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
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

    public void offerTo(RecipeExporter exporter, Identifier id) {
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + id);
        exporter.accept(id, new TransformCropsRecipe(target, fuel, output), exporter.getAdvancementBuilder()
                .criterion("has_the_recipe", RecipeUnlockedCriterion.create(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .criteriaMerger(AdvancementRequirements.CriterionMerger.OR)
                .build(id.withPrefixedPath("recipes/" + category.getName() + "/")));
    }

    public void offerTo(RecipeExporter exporter) {
        offerTo(exporter, Registries.BLOCK.getId(output.getBlock()));
    }

    public void offerTo(RecipeExporter exporter, String recipePath) {
        Identifier recipeId = new Identifier(recipePath);
        Identifier id = Registries.BLOCK.getId(output.getBlock());
        if (recipeId.equals(id)) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        offerTo(exporter, recipeId);
    }
}
