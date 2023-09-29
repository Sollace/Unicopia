package com.minelittlepony.unicopia.item;

import java.util.List;

import com.google.gson.JsonArray;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public interface URecipes {
    RecipeType<SpellbookRecipe> SPELLBOOK = RecipeType.register("unicopia:spellbook");

    RecipeSerializer<ZapAppleRecipe> ZAP_APPLE_SERIALIZER = RecipeSerializer.register("unicopia:crafting_zap_apple", new ZapAppleRecipe.Serializer());
    RecipeSerializer<GlowingRecipe> GLOWING_SERIALIZER = RecipeSerializer.register("unicopia:crafting_glowing", new SpecialRecipeSerializer<>(GlowingRecipe::new));
    RecipeSerializer<JarInsertRecipe> JAR_INSERT_SERIALIZER = RecipeSerializer.register("unicopia:jar_insert", new SpecialRecipeSerializer<>(JarInsertRecipe::new));
    RecipeSerializer<JarExtractRecipe> JAR_EXTRACT_SERIALIZER = RecipeSerializer.register("unicopia:jar_extract", new SpecialRecipeSerializer<>(JarExtractRecipe::new));
    RecipeSerializer<ShapedRecipe> CRAFTING_MAGICAL_SERIALIZER = RecipeSerializer.register("unicopia:crafting_magical", new SpellShapedCraftingRecipe.Serializer());
    RecipeSerializer<SpellCraftingRecipe> TRAIT_REQUIREMENT = RecipeSerializer.register("unicopia:spellbook/crafting", new SpellCraftingRecipe.Serializer());
    RecipeSerializer<SpellEnhancingRecipe> TRAIT_COMBINING = RecipeSerializer.register("unicopia:spellbook/combining", new SpellEnhancingRecipe.Serializer());
    RecipeSerializer<SpellDuplicatingRecipe> SPELL_DUPLICATING = RecipeSerializer.register("unicopia:spellbook/duplicating", new SpellDuplicatingRecipe.Serializer());

    Codec<DefaultedList<Ingredient>> SHAPELESS_RECIPE_INGREDIENTS_CODEC = Ingredient.DISALLOW_EMPTY_CODEC.listOf().flatXmap(ingredients -> {
        Ingredient[] ingredients2 = ingredients.stream().filter(ingredient -> !ingredient.isEmpty()).toArray(Ingredient[]::new);
        if (ingredients2.length == 0) {
            return DataResult.error(() -> "No ingredients for shapeless recipe");
        }
        if (ingredients2.length > 9) {
            return DataResult.error(() -> "Too many ingredients for shapeless recipe");
        }
        return DataResult.success(DefaultedList.copyOf(Ingredient.EMPTY, ingredients2));
    }, DataResult::success);

    static void bootstrap() {
        LootTableEvents.MODIFY.register((res, manager, id, supplier, setter) -> {
            if (!"minecraft".contentEquals(id.getNamespace())) {
                return;
            }

            Identifier modId = new Identifier("unicopiamc", id.getPath());
            LootTable table = manager.getLootTable(modId);

            if (table != LootTable.EMPTY) {
                if (table.getType() == LootContextTypes.ARCHAEOLOGY) {
                    supplier.modifyPools(poolBuilder -> {
                        for (LootPool pool : table.pools) {
                            poolBuilder.with(pool.entries);
                        }
                    });
                } else {
                    supplier.pools(table.pools);
                }
            }
        });
    }
}