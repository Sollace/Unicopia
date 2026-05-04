package com.minelittlepony.unicopia.client.gui.spellbook.element;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.client.gui.spellbook.IngredientTree;
import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement;

record Stack (ChapterPageElement.Stack stack, Bounds bounds) implements PageElement {
    public Stack(ChapterPageElement.Stack stack) {
        this(stack, Bounds.empty());
        bounds().copy(stack.bounds());
    }
    @Override
    public void compile(DynamicContent.GuiPage page, int y, IViewRoot container) {
        IngredientTree tree = new IngredientTree(
                bounds().left + page.getBounds().left,
                bounds().top + page.getBounds().top + y - 10,
                30
        );
        tree.input(stack.ingredient().getMatchingStacks());
        bounds().height = tree.build(container) - 10;
    }
}