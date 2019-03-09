package com.minelittlepony.unicopia.jei;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.enchanting.SpecialRecipe;
import com.minelittlepony.unicopia.enchanting.SpellRecipe;
import com.minelittlepony.unicopia.init.UItems;
import com.minelittlepony.unicopia.spell.SpellRegistry;

import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;

@JEIPlugin
public class JEIUnicopia implements IModPlugin {

    static IGuiHelper GUI_HELPER;

    static final String RECIPE_UID = "unicopia:spellbook_2";

    @Override
    public void registerItemSubtypes(ISubtypeRegistry registry) {
        registry.registerSubtypeInterpreter(UItems.spell, SpellRegistry::getKeyFromStack);
        registry.registerSubtypeInterpreter(UItems.curse, SpellRegistry::getKeyFromStack);
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        registry.addRecipeCategories(new SpellbookCategory());
    }

    @Override
    public void register(IModRegistry registry) {
        GUI_HELPER = registry.getJeiHelpers().getGuiHelper();

        registry.handleRecipes(SpellRecipe.class, SpellRecipeWrapper::new, RECIPE_UID);
        registry.handleRecipes(SpecialRecipe.class, SpellRecipeWrapper::new, RECIPE_UID);
        registry.addRecipes(Unicopia.getCraftingManager().getRecipes(), RECIPE_UID);
    }
}
