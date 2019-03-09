package com.minelittlepony.unicopia.jei;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.inventory.gui.GuiSpellBook;

import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeCategory;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.util.ResourceLocation;

public class SpellbookCategory implements IRecipeCategory<IRecipeWrapper> {

    @Override
    public String getUid() {
        return JEIUnicopia.RECIPE_UID;
    }

    @Override
    public String getTitle() {
        return "Spellbook";
    }

    @Override
    public IDrawable getIcon() {
        return JEIUnicopia.GUI_HELPER.drawableBuilder(
                new ResourceLocation(Unicopia.MODID, "textures/items/spellbook.png"), 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    @Override
    public String getModName() {
        return "Unicopia";
    }

    @Override
    public IDrawable getBackground() {
        return new BlendedDrawable(
                JEIUnicopia.GUI_HELPER.drawableBuilder(GuiSpellBook.spellBookGuiTextures, 405, 0, 105, 108)
                .setTextureSize(512, 256)
                .build());
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, IRecipeWrapper recipeWrapper, @Deprecated IIngredients unused) {
        recipeLayout.setShapeless();

        IGuiItemStackGroup stacks = recipeLayout.getItemStacks();

        stacks.init(0, true, 29, 3);
        stacks.init(1, true, 3, 46);
        stacks.init(2, true, 30, 86);
        stacks.init(3, true, 80, 72);
        stacks.init(4, true, 82, 15);

        stacks.init(5, false, 46, 44);

        stacks.set(unused);

        stacks.set(5, unused.getOutputs(VanillaTypes.ITEM).get(0));
    }
}
