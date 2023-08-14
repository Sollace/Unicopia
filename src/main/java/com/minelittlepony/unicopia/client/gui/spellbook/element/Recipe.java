package com.minelittlepony.unicopia.client.gui.spellbook.element;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.ability.magic.spell.crafting.SpellbookRecipe;
import com.minelittlepony.unicopia.client.gui.spellbook.IngredientTree;
import com.minelittlepony.unicopia.client.gui.spellbook.SpellbookScreen;
import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

record Recipe (DynamicContent.Page page, Identifier id, Bounds bounds) implements PageElement {
    @Override
    public void compile(int y, IViewRoot container) {
        if (container instanceof SpellbookScreen book) {
            bounds().left = book.getX();
            bounds().top = book.getY();
        }
        MinecraftClient.getInstance().world.getRecipeManager().get(id).ifPresent(recipe -> {
            if (recipe instanceof SpellbookRecipe spellRecipe) {

                boolean needsMoreXp = page.getLevel() < 0 || Pony.of(MinecraftClient.getInstance().player).getLevel().get() < page.getLevel();

                IngredientTree tree = new IngredientTree(
                        bounds().left + page().getBounds().left,
                        bounds().top + page().getBounds().top + y + 10,
                        page().getBounds().width - 20
                ).obfuscateResult(needsMoreXp);
                spellRecipe.buildCraftingTree(tree);
                bounds.height = tree.build(container) - 10;
            }
        });
    }
}
