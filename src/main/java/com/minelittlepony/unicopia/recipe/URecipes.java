package com.minelittlepony.unicopia.recipe;

import java.util.List;

import com.google.gson.JsonArray;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.*;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootTable;
import net.minecraft.recipe.CuttingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public interface URecipes {
    RecipeType<SpellbookRecipe> SPELLBOOK = RecipeType.register("unicopia:spellbook");
    RecipeType<StonecuttingRecipe> CLOUD_SHAPING = RecipeType.register("unicopia:cloud_shaping");
    RecipeType<TransformCropsRecipe> GROWING = RecipeType.register("unicopia:growing");

    RecipeSerializer<ShapelessRecipe> ZAP_APPLE_SERIALIZER = RecipeSerializer.register("unicopia:crafting_zap_apple", new ZapAppleRecipe.Serializer());
    RecipeSerializer<GlowingRecipe> GLOWING_SERIALIZER = RecipeSerializer.register("unicopia:crafting_glowing", new SpecialRecipeSerializer<>(GlowingRecipe::new));
    RecipeSerializer<JarInsertRecipe> JAR_INSERT_SERIALIZER = RecipeSerializer.register("unicopia:jar_insert", new SpecialRecipeSerializer<>(JarInsertRecipe::new));
    RecipeSerializer<JarExtractRecipe> JAR_EXTRACT_SERIALIZER = RecipeSerializer.register("unicopia:jar_extract", new SpecialRecipeSerializer<>(JarExtractRecipe::new));
    RecipeSerializer<ShapedRecipe> CRAFTING_MAGICAL_SERIALIZER = RecipeSerializer.register("unicopia:crafting_magical", new SpellShapedCraftingRecipe.Serializer());
    RecipeSerializer<SpellCraftingRecipe> TRAIT_REQUIREMENT = RecipeSerializer.register("unicopia:spellbook/crafting", new SpellCraftingRecipe.Serializer());
    RecipeSerializer<SpellEnhancingRecipe> TRAIT_COMBINING = RecipeSerializer.register("unicopia:spellbook/combining", new SpellEnhancingRecipe.Serializer());
    RecipeSerializer<SpellDuplicatingRecipe> SPELL_DUPLICATING = RecipeSerializer.register("unicopia:spellbook/duplicating", new SpellDuplicatingRecipe.Serializer());
    RecipeSerializer<CloudShapingRecipe> CLOUD_SHAPING_SERIALIZER = RecipeSerializer.register("unicopia:cloud_shaping", new CuttingRecipe.Serializer<>(CloudShapingRecipe::new) {});
    RecipeSerializer<TransformCropsRecipe> TRANSFORM_CROP_SERIALIZER = RecipeSerializer.register("unicopia:transform_crop", new TransformCropsRecipe.Serializer());

    static DefaultedList<Ingredient> getIngredients(JsonArray json) {
        DefaultedList<Ingredient> defaultedList = DefaultedList.of();

        for (int i = 0; i < json.size(); ++i) {
            Ingredient ingredient = Ingredient.fromJson(json.get(i));
            if (!ingredient.isEmpty()) {
                defaultedList.add(ingredient);
            }
        }

        return defaultedList;
    }

    static void bootstrap() {
        LootTableEvents.MODIFY.register((res, manager, id, supplier, setter) -> {
            if (!"minecraft".contentEquals(id.getNamespace())) {
                return;
            }

            Identifier modId = new Identifier("unicopiamc", id.getPath());
            LootTable table = manager.getLootTable(modId);

            if (table != LootTable.EMPTY) {
                if (id.getPath().startsWith("blocks/")) {
                    for (var pool : table.pools) {
                        supplier.pool(pool);
                    }
                } else {
                    supplier.modifyPools(poolBuilder -> {
                        for (var pool : table.pools) {
                            poolBuilder.with(List.of(pool.entries));
                        }
                    });
                }
            }
        });
    }
}