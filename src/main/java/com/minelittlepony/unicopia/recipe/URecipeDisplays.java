package com.minelittlepony.unicopia.recipe;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipeDisplay;

import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public interface URecipeDisplays {

    static void bootstrap() {
        register("spellbook", SpellbookRecipeDisplay.SERIALIZER);
    }

    static <T extends RecipeDisplay> void register(String name, RecipeDisplay.Serializer<T> serializer) {
        Registry.register(Registries.RECIPE_DISPLAY, Unicopia.id(name), serializer);
    }
}
