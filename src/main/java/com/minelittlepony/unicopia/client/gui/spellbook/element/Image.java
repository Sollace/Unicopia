package com.minelittlepony.unicopia.client.gui.spellbook.element;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.container.spellbook.ChapterPageElement;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;

record Image(ChapterPageElement.Image image) implements PageElement {
    @Override
    public void draw(DynamicContent.GuiPage page, DrawContext context, int mouseX, int mouseY, IViewRoot container) {
        context.drawTexture(RenderLayer::getGuiTextured, image().texture(), 0, 0, 0, 0, 0, bounds().width, bounds().height, bounds().width, bounds().height);
    }

    @Override
    public Bounds bounds() {
        return image.bounds();
    }
}
