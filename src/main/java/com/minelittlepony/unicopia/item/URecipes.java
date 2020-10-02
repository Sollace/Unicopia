package com.minelittlepony.unicopia.item;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface URecipes {

    RecipeSerializer<ShapelessRecipe> ZAP_APPLE_SERIALIZER = register("crafting_zap_apple", new ZapAppleRecipe.Serializer());

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

    static void bootstrap() {}
}