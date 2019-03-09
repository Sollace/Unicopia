package com.minelittlepony.unicopia.jei;

import java.util.List;
import java.util.stream.Collectors;

import com.minelittlepony.unicopia.enchanting.AbstractSpecialRecipe;

import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;

public class SpellRecipeWrapper implements IRecipeWrapper {

    private final AbstractSpecialRecipe recipe;

    public SpellRecipeWrapper(AbstractSpecialRecipe recipe) {
        this.recipe = recipe;
    }

    public AbstractSpecialRecipe getRecipe() {
        return recipe;
    }

    @Override
    public void getIngredients(IIngredients ingredients) {

        List<List<ItemStack>> ingreds = recipe.getSpellIngredients().stream().map(ingredient -> {
            return ingredient.getStacks().collect(Collectors.toList());
        }).collect(Collectors.toList());

        ingredients.setInputLists(VanillaTypes.ITEM, ingreds);

        ingredients.setOutput(VanillaTypes.ITEM, recipe.getRecipeOutput());
    }

}
