package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.List;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;

import net.minecraft.client.gui.screen.recipebook.GhostRecipe;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget.Tab;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.context.ContextParameterMap;

public class SpellbookRecipeBookWidget extends RecipeBookWidget<SpellbookScreenHandler> {
    private static final Text TOGGLE_CRAFTABLE_TEXT = Text.translatable("gui.recipebook.toggleRecipes.craftable");

    public SpellbookRecipeBookWidget(SpellbookScreenHandler craftingScreenHandler, List<Tab> tabs) {
        super(craftingScreenHandler, tabs);
    }

    @Override
    protected void setBookButtonTexture() {
    }

    @Override
    protected boolean isValid(Slot slot) {
        return false;
    }

    @Override
    protected void populateRecipes(RecipeResultCollection recipeResultCollection, RecipeFinder recipeFinder) {
    }

    @Override
    protected Text getToggleCraftableButtonText() {
        return null;
    }

    @Override
    protected void showGhostRecipe(GhostRecipe ghostRecipe, RecipeDisplay display, ContextParameterMap context) {
        // TODO Auto-generated method stub

    }
}