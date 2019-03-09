package com.minelittlepony.unicopia.jei;

import mezz.jei.api.gui.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

class BlendedDrawable implements IDrawable {

    private final IDrawable wrapped;

    public BlendedDrawable(IDrawable wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public int getWidth() {
        return wrapped.getWidth();
    }

    @Override
    public int getHeight() {
        return wrapped.getHeight();
    }

    @Override
    public void draw(Minecraft minecraft, int xOffset, int yOffset) {
        GlStateManager.enableBlend();
        wrapped.draw(minecraft, xOffset, yOffset);
        GlStateManager.disableBlend();
    }

}
