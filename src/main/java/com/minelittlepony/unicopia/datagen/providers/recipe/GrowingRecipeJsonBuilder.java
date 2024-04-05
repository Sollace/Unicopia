package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.Objects;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.mojang.serialization.JsonOps;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class GrowingRecipeJsonBuilder {
    private final Advancement.Builder advancementBuilder = Advancement.Builder.createUntelemetered();
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

    public GrowingRecipeJsonBuilder criterion(String name, CriterionConditions condition) {
        advancementBuilder.criterion(name, condition);
        return this;
    }

    public GrowingRecipeJsonBuilder group(String group) {
        this.group = group;
        return this;
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier id) {
        Preconditions.checkState(!advancementBuilder.getCriteria().isEmpty(), "No way of obtaining recipe " + id);
        advancementBuilder
            .parent(CraftingRecipeJsonBuilder.ROOT)
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .criteriaMerger(CriterionMerger.OR);
        exporter.accept(new JsonProvider(id, id.withPrefixedPath("recipes/" + category.getName() + "/"), group, target, fuel));
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter) {
        offerTo(exporter, Registries.BLOCK.getId(output.getBlock()));
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter, String recipePath) {
        Identifier recipeId = new Identifier(recipePath);
        Identifier id = Registries.BLOCK.getId(output.getBlock());
        if (recipeId.equals(id)) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        offerTo(exporter, recipeId);
    }

    private class JsonProvider implements RecipeJsonProvider {
        private final Identifier recipeId;
        private final Identifier advancementId;
        private final String group;
        private final Block target;
        private final BlockState fuel;

        JsonProvider(Identifier recipeId, Identifier advancementId, String group, Block target, BlockState fuel) {
            this.recipeId = recipeId;
            this.advancementId = advancementId;
            this.group = group;
            this.target = Objects.requireNonNull(target, "Target");
            this.fuel = Objects.requireNonNull(fuel, "Fuel");
        }

        @Override
        public void serialize(JsonObject json) {
            json.addProperty("group", group);
            json.addProperty("target", Registries.BLOCK.getId(target).toString());
            json.add("consume", BlockState.CODEC.encodeStart(JsonOps.INSTANCE, fuel).result().get());
            json.add("output", BlockState.CODEC.encodeStart(JsonOps.INSTANCE, output).result().get());
        }

        @Override
        public Identifier getRecipeId() {
            return recipeId;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return URecipes.TRANSFORM_CROP_SERIALIZER;
        }

        @Override
        public JsonObject toAdvancementJson() {
            return advancementBuilder.toJson();
        }

        @Override
        public Identifier getAdvancementId() {
            return advancementId;
        }
    }
}
