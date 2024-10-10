package com.minelittlepony.unicopia.datagen.providers.recipe;

import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.recipe.ZapAppleRecipe;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementCriterion;
import net.minecraft.advancement.AdvancementRequirements;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.criterion.RecipeUnlockedCriterion;
import net.minecraft.data.server.recipe.CraftingRecipeJsonBuilder;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.CraftingRecipeCategory;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class TrickCraftingRecipeJsonBuilder implements CraftingRecipeJsonBuilder {
    private final Map<String, AdvancementCriterion<?>> criterions = new LinkedHashMap<>();
    @Nullable
    private String group;
    private final RecipeCategory category;
    private final Item output;

    private final DefaultedList<Ingredient> inputs = DefaultedList.of();

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
        exporter.accept(id,
                new ZapAppleRecipe(group == null ? "" : group, CraftingRecipeCategory.MISC, UItems.ZAP_APPLE.setAppearance(UItems.ZAP_APPLE.getDefaultStack(), output.getDefaultStack()), inputs),
                builder.build(id.withPrefixedPath("recipes/" + category.getName() + "/"))
        );
    }
}
