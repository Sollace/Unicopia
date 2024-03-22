package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.item.URecipes;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class TrickCraftingRecipeJsonBuilder extends RecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final Advancement.Builder advancementBuilder = Advancement.Builder.createUntelemetered();
    @Nullable
    private String group;
    private final RecipeCategory category;
    private final Item output;

    private final List<Ingredient> inputs = new ArrayList<>();

    public TrickCraftingRecipeJsonBuilder(RecipeCategory category, ItemConvertible output) {
        this.category = category;
        this.output = output.asItem();
    }

    public static TrickCraftingRecipeJsonBuilder create(RecipeCategory category, ItemConvertible output) {
        return new TrickCraftingRecipeJsonBuilder(category, output);
    }

    @Override
    public Item getOutputItem() {
        return output;
    }

    public TrickCraftingRecipeJsonBuilder input(ItemConvertible input) {
        inputs.add(Ingredient.ofItems(input));
        return this;
    }

    @Override
    public TrickCraftingRecipeJsonBuilder criterion(String name, CriterionConditions condition) {
        advancementBuilder.criterion(name, condition);
        return this;
    }

    @Override
    public TrickCraftingRecipeJsonBuilder group(String group) {
        this.group = group;
        return this;
    }

    @Override
    public void offerTo(Consumer<RecipeJsonProvider> exporter, Identifier id) {
        Preconditions.checkState(!advancementBuilder.getCriteria().isEmpty(), "No way of obtaining recipe " + id);
        advancementBuilder
            .parent(CraftingRecipeJsonBuilder.ROOT)
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .criteriaMerger(CriterionMerger.OR);
        exporter.accept(new JsonProvider(id, id.withPrefixedPath("recipes/" + category.getName() + "/"), group == null ? "" : group, inputs));
    }

    private class JsonProvider extends RecipeJsonBuilder.CraftingRecipeJsonProvider {
        private final Identifier recipeId;
        private final String group;
        private final List<Ingredient> inputs;
        private final Identifier advancementId;

        protected JsonProvider(Identifier recipeId, Identifier advancementId, String group, List<Ingredient> inputs) {
            super(CraftingRecipeCategory.MISC);
            this.recipeId = recipeId;
            this.advancementId = advancementId;
            this.group = group;
            this.inputs = new ArrayList<>(inputs);
        }

        @Override
        public void serialize(JsonObject json) {
            super.serialize(json);
            if (!group.isEmpty()) {
                json.addProperty("group", group);
            }
            JsonArray jsonArray = new JsonArray();
            for (Ingredient ingredient : inputs) {
                jsonArray.add(ingredient.toJson());
            }
            json.add("ingredients", jsonArray);
            json.addProperty("appearance", Registries.ITEM.getId(output).toString());
        }

        @Override
        public RecipeSerializer<?> getSerializer() {
            return URecipes.ZAP_APPLE_SERIALIZER;
        }

        @Override
        public Identifier getRecipeId() {
            return this.recipeId;
        }

        @Override
        @Nullable
        public JsonObject toAdvancementJson() {
            return advancementBuilder.toJson();
        }

        @Override
        @Nullable
        public Identifier getAdvancementId() {
            return advancementId;
        }
    }
}
