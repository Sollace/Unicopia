package com.minelittlepony.unicopia.item;

import com.google.gson.JsonArray;

import net.fabricmc.fabric.api.loot.v1.FabricLootSupplier;
import net.fabricmc.fabric.api.loot.v1.event.LootTableLoadingCallback;
import net.minecraft.loot.LootTable;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;

public interface URecipes {

    RecipeSerializer<ShapelessRecipe> ZAP_APPLE_SERIALIZER = register("crafting_zap_apple", new ZapAppleRecipe.Serializer());
    RecipeSerializer<GlowingRecipe> GLOWING_SERIALIZER = register("crafting_glowing", new SpecialRecipeSerializer<>(GlowingRecipe::new));
    RecipeSerializer<JarInsertRecipe> JAR_INSERT_SERIALIZER = register("jar_insert", new SpecialRecipeSerializer<>(JarInsertRecipe::new));

    static <T extends Recipe<?>> RecipeType<T> register(final String id) {
        return Registry.register(Registry.RECIPE_TYPE, new Identifier("unicopia", id), new RecipeType<T>() {
            @Override
            public String toString() {
                return id;
            }
        });
    }

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S register(String id, S serializer) {
        return Registry.register(Registry.RECIPE_SERIALIZER, new Identifier("unicopia", id), serializer);
    }


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