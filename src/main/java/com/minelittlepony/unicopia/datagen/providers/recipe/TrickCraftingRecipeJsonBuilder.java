package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.recipe.URecipes;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class TrickCraftingRecipeJsonBuilder extends RecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final Map<String, AdvancementCriterion<?>> criterions = new LinkedHashMap<>();
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
    public TrickCraftingRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> condition) {
        criterions.put(name, condition);
        return this;
    }

    @Override
    public TrickCraftingRecipeJsonBuilder group(String group) {
        this.group = group;
        return this;
    }

    @Override
    public void offerTo(RecipeExporter exporter, Identifier id) {
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + id);
        Advancement.Builder builder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        criterions.forEach(builder::criterion);
        exporter.accept(new JsonProvider(id, group == null ? "" : group, inputs, builder.build(id.withPrefixedPath("recipes/" + category.getName() + "/"))));
    }

    private class JsonProvider extends RecipeJsonBuilder.CraftingRecipeJsonProvider {
        private final Identifier recipeId;
        private final AdvancementEntry advancement;
        private final String group;
        private final List<Ingredient> inputs;

        protected JsonProvider(Identifier recipeId, String group, List<Ingredient> inputs, AdvancementEntry advancement) {
            super(CraftingRecipeCategory.MISC);
            this.recipeId = recipeId;
            this.advancement = advancement;
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
            inputs.forEach(i -> jsonArray.add(i.toJson(false)));
            json.add("ingredients", jsonArray);
            json.addProperty("appearance", Registries.ITEM.getId(output).toString());
        }

        @Override
        public RecipeSerializer<?> serializer() {
            return URecipes.ZAP_APPLE_SERIALIZER;
        }

        @Override
        public Identifier id() {
            return this.recipeId;
        }

        @Override
        @Nullable
        public AdvancementEntry advancement() {
            return advancement;
        }
    }
}
