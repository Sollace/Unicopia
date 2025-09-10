package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.List;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler;
import com.minelittlepony.unicopia.container.inventory.IngredientSlot;

import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.recipebook.GhostRecipe;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.display.RecipeDisplay;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextParameterMap;

public class SpellbookRecipeBookWidget extends RecipeBookWidget<SpellbookScreenHandler> {
    private static final Text TOGGLE_CRAFTABLE_TEXT = Text.translatable("gui.recipebook.toggleRecipes.craftable");
    private static final ButtonTextures TEXTURES = new ButtonTextures(
        Identifier.ofVanilla("recipe_book/filter_enabled"),
        Identifier.ofVanilla("recipe_book/filter_disabled"),
        Identifier.ofVanilla("recipe_book/filter_enabled_highlighted"),
        Identifier.ofVanilla("recipe_book/filter_disabled_highlighted")
    );

    public SpellbookRecipeBookWidget(SpellbookScreenHandler craftingScreenHandler) {
        super(craftingScreenHandler, List.of());
    }

    @Override
    protected void setBookButtonTexture() {
        toggleCraftableButton.setTextures(TEXTURES);
    }

    @Override
    protected boolean isValid(Slot slot) {
        return slot instanceof IngredientSlot;
    }

    @Override
    protected void populateRecipes(RecipeResultCollection recipeResultCollection, RecipeFinder recipeFinder) {
        recipeResultCollection.populateRecipes(recipeFinder, recipe -> {
            return false;
        });
    }

    @Override
    protected Text getToggleCraftableButtonText() {
        return TOGGLE_CRAFTABLE_TEXT;
    }

    @Override
    protected void showGhostRecipe(GhostRecipe ghostRecipe, RecipeDisplay display, ContextParameterMap context) {
        // TODO Auto-generated method stub

    }
}