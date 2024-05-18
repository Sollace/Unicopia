package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class CuttingBoardRecipeJsonBuilder {
    private final Advancement.Builder advancementBuilder = Advancement.Builder.createUntelemetered();

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

    public CuttingBoardRecipeJsonBuilder criterion(String name, CriterionConditions condition) {
        advancementBuilder.criterion(name, condition);
        return this;
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier id) {
        id = id.withPrefixedPath("cutting/");
        Preconditions.checkState(!advancementBuilder.getCriteria().isEmpty(), "No way of obtaining recipe " + id);
        advancementBuilder
            .parent(CraftingRecipeJsonBuilder.ROOT)
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .criteriaMerger(CriterionMerger.OR);
        exporter.accept(new JsonProvider(id, id.withPrefixedPath("recipes/")));
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter) {
        offerTo(exporter, Registries.ITEM.getId(output.asItem()));
    }

    public void offerTo(Consumer<RecipeJsonProvider> exporter, String recipePath) {
        Identifier recipeId = new Identifier(recipePath);
        if (recipeId.equals(Registries.ITEM.getId(output.asItem()))) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        offerTo(exporter, recipeId);
    }

    private class JsonProvider implements RecipeJsonProvider {
        private final Identifier recipeId;
        private final Identifier advancementId;
        public JsonProvider(Identifier recipeId, Identifier advancementId) {
            this.recipeId = recipeId;
            this.advancementId = advancementId;
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
                ingredientsJson.add(ingredient.toJson());
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
        public Identifier getRecipeId() {
            return recipeId;
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return RecipeSerializer.SHAPELESS;
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
