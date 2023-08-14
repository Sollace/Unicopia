package com.minelittlepony.unicopia.client.gui.spellbook.element;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.container.SpellbookChapterLoader.Flow;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

record Image(
    Identifier texture,
    Bounds bounds,
    Flow flow) implements PageElement {
    @Override
    public void draw(DrawContext context, int mouseX, int mouseY, IViewRoot container) {
        context.drawTexture(texture, 0, 0, 0, 0, 0, bounds().width, bounds().height, bounds().width, bounds().height);
    }
}
