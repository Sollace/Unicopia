package com.minelittlepony.unicopia.recipe;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface URecipes {
    RecipeType<SpellBookRecipe> SPELL_BOOK = register("spell_book");

    RecipeSerializer<SpellBookRecipe> ENCHANTING_SPELL_SERIALIZER = register("enchanting_spell", new SpellBookRecipe.Serializer());
    RecipeSerializer<SpellBookRecipe> CRAFTING_SPELL_SERIALIZER = register("crafting_spell", new SpellBookRecipe.Serializer());

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
