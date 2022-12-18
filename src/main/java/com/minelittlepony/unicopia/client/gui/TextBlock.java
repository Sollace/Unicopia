package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.element.Label;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;

public class TextBlock extends Label {
    private final int maxWidth;

    public TextBlock(int x, int y, int width) {
        super(x, y);
        this.maxWidth = width;
        this.render(null, x, y, width);
    }

    @Override
    public Bounds getBounds() {
        Bounds bounds = super.getBounds();
        bounds.height = getFont().wrapLines(getStyle().getText(), maxWidth).size() * getFont().fontHeight;
        return bounds;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        int textY = (int)(getY() + MinecraftClient.getInstance().textRenderer.fontHeight/1.5F);

        for (OrderedText line : getFont().wrapLines(getStyle().getText(), maxWidth)) {
            getFont().drawWithShadow(matrices, line, getX(), textY, getStyle().getColor());
            textY += getFont().fontHeight;
        }
    }
}