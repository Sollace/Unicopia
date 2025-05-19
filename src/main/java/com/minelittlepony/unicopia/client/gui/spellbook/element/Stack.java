package com.minelittlepony.unicopia.client.gui.spellbook.element;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.IngredientWithSpell;
import com.minelittlepony.unicopia.client.gui.spellbook.IngredientTree;

record Stack (DynamicContent.Page page, IngredientWithSpell ingredient, Bounds bounds) implements PageElement {
    @Override
    public void compile(int y, IViewRoot container) {
        IngredientTree tree = new IngredientTree(
                bounds().left + page().getBounds().left,
                bounds().top + page().getBounds().top + y - 10,
                30
        );
        tree.input(ingredient.getMatchingStacks());
        bounds.height = tree.build(container) - 10;
    }
}