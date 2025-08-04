package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.Unicopia;

import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface URecipeBookCategories {
    RecipeBookCategory SPELLBOOK = register("spellbook");

    private static RecipeBookCategory register(String id) {
        return Registry.register(Registries.RECIPE_BOOK_CATEGORY, Unicopia.id(id), new RecipeBookCategory());
    }


    static void bootstrap() {}
}
