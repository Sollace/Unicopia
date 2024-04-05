package com.minelittlepony.unicopia.datagen.providers.recipe;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;

import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.item.ItemConvertible;
import net.minecraft.recipe.Recipe;

public interface ComplexSpellcraftingRecipeJsonBuilder {
    static ComplexSpellcraftingRecipeJsonBuilder create(Factory factory, ItemConvertible material) {
        return (exporter, recipeId) -> exporter.accept(Unicopia.id(recipeId), factory.create(IngredientWithSpell.mundane(material)), null);
    }

    public interface Factory {
        Recipe<?> create(IngredientWithSpell material);
    }

    void offerTo(RecipeExporter exporter, final String recipeId);
}
