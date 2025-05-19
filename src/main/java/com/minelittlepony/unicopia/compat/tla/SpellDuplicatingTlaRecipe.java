package com.minelittlepony.unicopia.compat.tla;

import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;

import io.github.mattidragon.tlaapi.api.recipe.TlaIngredient;
import net.minecraft.recipe.RecipeEntry;

class SpellDuplicatingTlaRecipe extends SpellbookTlaRecipe {
    public SpellDuplicatingTlaRecipe(RecipeEntry<SpellbookRecipe> recipe) {
        super(recipe);
    }

    @Override
    protected TlaIngredient getOutput() {
        return super.getOutput().withAmount(2);
    }
}
