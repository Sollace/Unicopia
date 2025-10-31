package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.AltarRecipe;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.collection.DefaultedList;

public class AltarRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final Map<String, AdvancementCriterion<?>> criterions = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final RecipeCategory category;

    private final DefaultedList<Ingredient> ingredients = DefaultedList.of();
    private final ItemConvertible result;

    public static AltarRecipeJsonBuilder create(RecipeCategory category, ItemConvertible result) {
        return new AltarRecipeJsonBuilder(category, result);
    }

    private AltarRecipeJsonBuilder(RecipeCategory category, ItemConvertible result) {
        this.category = category;
        this.result = result;
    }

    public AltarRecipeJsonBuilder input(ItemConvertible item) {
        ingredients.add(Ingredient.ofItems(item));
        return this;
    }

    @Override
    public AltarRecipeJsonBuilder criterion(String name, AdvancementCriterion<?> condition) {
        criterions.put(name, condition);
        return this;
    }

    @Override
    public AltarRecipeJsonBuilder group(String group) {
        this.group = group;
        return this;
    }

    @Override
    public void offerTo(RecipeExporter exporter, RegistryKey<Recipe<?>> key) {
        Preconditions.checkState(!criterions.isEmpty(), "No way of obtaining recipe " + key);
        Advancement.Builder advancementBuilder = exporter.getAdvancementBuilder()
            .criterion("has_the_recipe", RecipeUnlockedCriterion.create(key))
            .rewards(AdvancementRewards.Builder.recipe(key))
            .criteriaMerger(AdvancementRequirements.CriterionMerger.OR);
        criterions.forEach(advancementBuilder::criterion);
        exporter.accept(key, new AltarRecipe(
                group == null ? "" : group,
                ingredients,
                new ItemStack(result)
        ), advancementBuilder.build(key.getValue().withPrefixedPath("recipes/" + category.getName() + "/")));
    }

    @Override
    public Item getOutputItem() {
        return result.asItem();
    }
}
