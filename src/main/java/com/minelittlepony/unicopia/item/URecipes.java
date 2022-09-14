package com.minelittlepony.unicopia.item;

import com.google.gson.JsonArray;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.*;

import net.fabricmc.fabric.api.loot.v1.FabricLootSupplier;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.loot.LootTable;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public interface URecipes {
    RecipeType<SpellbookRecipe> SPELLBOOK = RecipeType.register("unicopia:spellbook");

    RecipeSerializer<ShapelessRecipe> ZAP_APPLE_SERIALIZER = RecipeSerializer.register("unicopia:crafting_zap_apple", new ZapAppleRecipe.Serializer());
    RecipeSerializer<GlowingRecipe> GLOWING_SERIALIZER = RecipeSerializer.register("unicopia:crafting_glowing", new SpecialRecipeSerializer<>(GlowingRecipe::new));
    RecipeSerializer<JarInsertRecipe> JAR_INSERT_SERIALIZER = RecipeSerializer.register("unicopia:jar_insert", new SpecialRecipeSerializer<>(JarInsertRecipe::new));
    RecipeSerializer<SpellCraftingRecipe> TRAIT_REQUIREMENT = RecipeSerializer.register("unicopia:spellbook/crafting", new SpellCraftingRecipe.Serializer());
    RecipeSerializer<SpellEnhancingRecipe> TRAIT_COMBINING = RecipeSerializer.register("unicopia:spellbook/combining", new SpellEnhancingRecipe.Serializer());
    RecipeSerializer<SpellDuplicatingRecipe> SPELL_DUPLICATING = RecipeSerializer.register("unicopia:spellbook/duplicating", new SpellDuplicatingRecipe.Serializer());

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
        LootTableLoadingCallback.EVENT.register((res, manager, id, supplier, setter) -> {
            if (!"minecraft".contentEquals(id.getNamespace())) {
                return;
            }

            Identifier modId = new Identifier("unicopiamc", id.getPath());
            LootTable table = manager.getTable(modId);
            if (table != LootTable.EMPTY) {
                supplier.withPools(((FabricLootSupplier)table).getPools());
            }
        });
    }
}