package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minelittlepony.unicopia.ability.magic.spell.effect.SpellType;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.advancement.TraitDiscoveredCriterion;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.URecipes;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

public class SpellcraftingRecipeJsonBuilder {
    private final Map<String, AdvancementCriterion<?>> criterions = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final RecipeCategory category;

    private EnchantedIngredient base = new EnchantedIngredient(UItems.GEMSTONE, SpellType.EMPTY_KEY);
    private final EnchantedIngredient output;
    private final List<EnchantedIngredient> ingredients = new ArrayList<>();
    private SpellTraits traits = SpellTraits.EMPTY;

    public static SpellcraftingRecipeJsonBuilder create(RecipeCategory category, ItemConvertible gem, SpellType<?> spell) {
        return new SpellcraftingRecipeJsonBuilder(category, new EnchantedIngredient(gem, spell));
    }

    protected SpellcraftingRecipeJsonBuilder(RecipeCategory category, EnchantedIngredient output) {
        this.category = category;
        this.output = output;
    }

    public SpellcraftingRecipeJsonBuilder base(ItemConvertible gem, SpellType<?> spell) {
        base = new EnchantedIngredient(gem, spell);
        return this;
    }

    public SpellcraftingRecipeJsonBuilder input(ItemConvertible gem, SpellType<?> spell) {
        ingredients.add(new EnchantedIngredient(gem, spell));
        return this;
    }

    public SpellcraftingRecipeJsonBuilder traits(SpellTraits.Builder traits) {
        this.traits = traits.build();
        return this;
    }

    public SpellcraftingRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> condition) {
        criterions.put(name, condition);
        return this;
    }

    public SpellcraftingRecipeJsonBuilder group(String group) {
        this.group = group;
        return this;
    }

    public void offerTo(RecipeExporter exporter, Identifier id) {

        if (!traits.isEmpty()) {
            criterions.put("has_traits", TraitDiscoveredCriterion.create(traits.stream().map(Map.Entry::getKey).collect(Collectors.toUnmodifiableSet())));
        }
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + id);
        Advancement.Builder advancementBuilder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(id))
            .rewards(AdvancementRewards.Builder.recipe(id))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        exporter.accept(new JsonProvider(id, group, ingredients, traits, advancementBuilder.build(id.withPrefixedPath("recipes/" + category.getName() + "/"))));
    }

    public void offerTo(RecipeExporter exporter) {
        offerTo(exporter, output.spell().getId());
    }

    public void offerTo(RecipeExporter exporter, String recipePath) {
        Identifier recipeId = new Identifier(recipePath);
        if (recipeId.equals(output.spell().getId())) {
            throw new IllegalStateException("Recipe " + recipePath + " should remove its 'save' argument as it is equal to default one");
        }
        offerTo(exporter, recipeId);
    }

    record EnchantedIngredient (ItemConvertible gem, SpellType<?> spell) {
        JsonObject toJson(JsonObject json) {
            json.addProperty("item", CraftingRecipeJsonBuilder.getItemId(gem).toString());
            if (!spell.isEmpty()) {
                json.addProperty("spell", spell.getId().toString());
            }
            return json;
        }
    }

    private class JsonProvider implements RecipeJsonProvider {
        private final Identifier recipeId;
        private final AdvancementEntry advancement;
        private final String group;
        private final List<EnchantedIngredient> ingredients;
        private final SpellTraits traits;

        JsonProvider(Identifier recipeId, String group, List<EnchantedIngredient> ingredients, SpellTraits traits, AdvancementEntry advancement) {
            this.recipeId = recipeId;
            this.advancement = advancement;
            this.group = group;
            this.ingredients = new ArrayList<>(ingredients);
            this.traits = traits;
        }

        @Override
        public void serialize(JsonObject json) {
            json.addProperty("group", group);
            json.add("material", base.toJson(new JsonObject()));
            json.add("result", output.toJson(new JsonObject()));
            JsonArray ingredientsJson = new JsonArray();
            ingredients.forEach(ingredient -> {
                ingredientsJson.add(ingredient.toJson(new JsonObject()));
            });
            json.add("ingredients", ingredientsJson);
            JsonObject traitsJson = new JsonObject();
            traits.forEach(entry -> {
                traitsJson.addProperty(entry.getKey().getId().toString(), entry.getValue());
            });
            json.add("traits", traitsJson);
        }

        @Override
        public Identifier id() {
            return recipeId;
        }

        @Override
        public RecipeSerializer<?> serializer() {
            return URecipes.TRAIT_REQUIREMENT;
        }

        @Override
        public AdvancementEntry advancement() {
            return advancement;
        }
    }
}
