package com.minelittlepony.unicopia.client.gui.spellbook.element;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipeDisplay;
import com.minelittlepony.unicopia.client.gui.spellbook.IngredientTree;
import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;

record Recipe (ChapterPageElement.Recipe recipe, Bounds bounds) implements PageElement {
    public Recipe(ChapterPageElement.Recipe recipe) {
        this(recipe, Bounds.empty());
    }

    @Override
    public void compile(DynamicContent.GuiPage page, int y, IViewRoot container) {
        boolean needsMoreXp = page.getLevel() < 0 || Pony.of(MinecraftClient.getInstance().player).getLevel().get() < page.getLevel();
        bounds.height = 0;
        recipe.recipeDisplays().forEach(display -> {
            IngredientTree tree = new IngredientTree(
                    bounds().left,
                    bounds().top + y - 10,
                    page.getBounds().width - 20
            ).obfuscateResult(needsMoreXp);
            ((SpellbookRecipeDisplay)display).buildCraftingTree(tree);
            bounds.height += tree.build(container) - 10;
        });
    }
}
