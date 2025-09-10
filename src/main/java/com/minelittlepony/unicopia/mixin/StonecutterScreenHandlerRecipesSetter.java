package com.minelittlepony.unicopia.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.display.CuttingRecipeDisplay;
import net.minecraft.screen.StonecutterScreenHandler;

@Mixin(StonecutterScreenHandler.class)
public interface StonecutterScreenHandlerRecipesSetter {
    @Accessor
    void setAvailableRecipes(CuttingRecipeDisplay.Grouping<? extends StonecuttingRecipe> availableRecipes);
}
