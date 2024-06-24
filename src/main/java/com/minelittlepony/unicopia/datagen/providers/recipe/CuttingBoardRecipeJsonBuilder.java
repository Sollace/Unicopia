/*
package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class CuttingBoardRecipeJsonBuilder {
    private final Map<String, AdvancementCriterion<?>> criterions = new LinkedHashMap<>();

    private final ItemConvertible output;
    private final TagKey<Item> tool;

    private final List<Supplier<Identifier>> results = new ArrayList<>();
    private final List<Ingredient> ingredients = new ArrayList<>();

    public static CuttingBoardRecipeJsonBuilder create(ItemConvertible output, TagKey<Item> tool) {
        return new CuttingBoardRecipeJsonBuilder(output, tool);
    }

    protected CuttingBoardRecipeJsonBuilder(ItemConvertible output, TagKey<Item> tool) {
        this.output = output;
        this.tool = tool;
        result(output);
    }

    public CuttingBoardRecipeJsonBuilder input(ItemConvertible input) {
        ingredients.add(Ingredient.ofItems(input));
        return this;
    }

    public CuttingBoardRecipeJsonBuilder result(ItemConvertible result) {
        results.add(() -> Registries.ITEM.getId(result.asItem()));
        return this;
    }

    public CuttingBoardRecipeJsonBuilder result(Identifier result) {
        results.add(() -> result);
        return this;
    }

    public CuttingBoardRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> condition) {
        criterions.put(name, condition);
        return this;
    }

    public void offerTo(RecipeExporter exporter, Identifier id) {
        id = id.withPrefixedPath("cutting/");
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + id);
        Advancement.Builder advancementBuilder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        exporter.accept(new JsonProvider(id, advancementBuilder.build(id.withPrefixedPath("recipes/"))));
    }

    public void offerTo(RecipeExporter exporter) {
        offerTo(exporter, Registries.ITEM.getId(output.asItem()));
    }

    public void offerTo(RecipeExporter exporter, String recipePath) {
        Identifier recipeId = new Identifier(recipePath);
        if (recipeId.equals(Registries.ITEM.getId(output.asItem()))) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        offerTo(exporter, recipeId);
    }

    private class JsonProvider implements RecipeJsonProvider {
        private final Identifier recipeId;
        private final AdvancementEntry advancement;
        public JsonProvider(Identifier recipeId, AdvancementEntry advancement) {
            this.recipeId = recipeId;
            this.advancement = advancement;
        }

        @Override
        public JsonObject toJson() {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("type", "farmersdelight:cutting");
            serialize(jsonObject);
            return jsonObject;
        }

        @Override
        public void serialize(JsonObject json) {
            JsonArray ingredientsJson = new JsonArray();
            for (var ingredient : ingredients) {
                ingredientsJson.add(ingredient.toJson(false));
            }
            json.add("ingredients", ingredientsJson);
            JsonObject toolJson = new JsonObject();
            toolJson.addProperty("type", "farmersdelight:tool");
            toolJson.addProperty("tag", tool.id().toString());
            json.add("tool", toolJson);
            JsonArray resultJson = new JsonArray();
            for (var result : results) {
                JsonObject o = new JsonObject();
                o.addProperty("item", result.get().toString());
                resultJson.add(o);
            }
            json.add("result", resultJson);
        }

        @Override
        public Identifier id() {
            return recipeId;
        }

        @Override
        public RecipeSerializer<?> serializer() {
            return RecipeSerializer.SHAPELESS;
        }

        @Override
        public AdvancementEntry advancement() {
            return advancement;
        }

    }
}
*/