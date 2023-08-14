package com.minelittlepony.unicopia.client.gui.spellbook.element;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.client.gui.spellbook.IngredientTree;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookScreen;

record Stack (DynamicContent.Page page, IngredientWithSpell ingredient, Bounds bounds) implements PageElement {
    @Override
    public void compile(int y, IViewRoot container) {
        int xx = 0, yy = 0;
        if (container instanceof SpellbookScreen book) {
            xx = book.getX();
            yy = book.getY();
        }
        IngredientTree tree = new IngredientTree(
                bounds().left + xx + page().getBounds().left,
                bounds().top + yy + page().getBounds().top + y + 10,
                30
        );
        tree.input(ingredient.getMatchingStacks());
        bounds.height = tree.build(container) - 10;
    }
}