package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.recipe.URecipes;
import com.mojang.serialization.JsonOps;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.recipe.RecipeSerializer;
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
        Advancement.Builder advancementBuilder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        exporter.accept(new JsonProvider(id, group, target, fuel, advancementBuilder.build(id.withPrefixedPath("recipes/" + category.getName() + "/"))));
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

    private class JsonProvider implements RecipeJsonProvider {
        private final Identifier recipeId;
        private final AdvancementEntry advancement;
        private final String group;
        private final Block target;
        private final BlockState fuel;

        JsonProvider(Identifier recipeId, String group, Block target, BlockState fuel, AdvancementEntry advancement) {
            this.recipeId = recipeId;
            this.advancement = advancement;
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
        public Identifier id() {
            return recipeId;
        }

        @Override
        public RecipeSerializer<?> serializer() {
            return URecipes.TRANSFORM_CROP_SERIALIZER;
        }

        @Override
        public AdvancementEntry advancement() {
            return advancement;
        }
    }
}
