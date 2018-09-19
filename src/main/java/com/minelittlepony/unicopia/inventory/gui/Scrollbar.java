package com.minelittlepony.unicopia.inventory.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.MathHelper;

public class Scrollbar {

    private boolean dragging = false;
    private boolean touching = false;

    private int scrollY = 0;

    private int thickness = 6;

    private float scrollMomentum = 0;
    private float scrollFactor = 0;

    private int maxScrollY = 0;
    private int shiftFactor = 0;

    private int elementHeight;
    private int contentHeight;

    private int x;
    private int y;

    private int initialMouseY;

    public Scrollbar() {

    }

    public void reposition(int x, int y, int elementHeight, int contentHeight) {
        this.x = x;
        this.y = y;
        this.elementHeight = elementHeight;
        this.contentHeight = contentHeight;

        maxScrollY = contentHeight - elementHeight;
        if (maxScrollY < 0) {
            maxScrollY = 0;
        }
        scrollFactor = elementHeight == 0 ? 1 : contentHeight / elementHeight;

        scrollBy(0);
    }

    public int getScrollAmount() {
        return scrollY;
    }

    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!touching && !dragging) {
            scrollMomentum *= partialTicks;
            if (scrollMomentum > 0) {
                scrollBy(scrollMomentum);
            }

            if (shiftFactor != 0) {
                scrollBy(shiftFactor * scrollFactor);
                shiftFactor = computeShiftFactor(mouseX, mouseY);
            }
        }

        if (maxScrollY <= 0) return;

        renderVertical();
    }

    protected void renderVertical() {
        int scrollbarHeight = getScrubberLength(elementHeight, contentHeight);
        int scrollbarTop = getScrubberStart(scrollbarHeight, elementHeight, contentHeight);

        renderBackground(y, x, y + elementHeight, x + thickness);
        renderBar(x, x + thickness, scrollbarTop, scrollbarTop + scrollbarHeight);
    }

    protected int getScrubberStart(int scrollbarHeight, int elementHeight, int contentHeight) {
        if (maxScrollY == 0) {
            return 0;
        }

        int scrollbarTop = y + getScrollAmount() * (elementHeight - scrollbarHeight) / maxScrollY;
        if (scrollbarTop < 0) {
            return 0;
        }
        return scrollbarTop;
    }

    protected int getScrubberLength(int elementL, int contentL) {
        return MathHelper.clamp(elementL * elementL / contentL, 32, elementL - 8);
    }

    public boolean isFocused(int mouseX, int mouseY) {
        return mouseY >= y
            && mouseY <= y + elementHeight
            && mouseX >= (x - 10)
            && mouseX <= (x + thickness + 1);
    }

    private void renderBackground(int top, int left, int bottom, int right) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableTexture2D();

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
        GlStateManager.disableAlpha();
        GlStateManager.shadeModel(7425);

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(left, bottom, 0).tex(0, 1).color(0, 0, 0, 150).endVertex();
        bufferbuilder.pos(right, bottom, 0).tex(1, 1).color(0, 0, 0, 150).endVertex();
        bufferbuilder.pos(right, top, 0).tex(1, 0).color(0, 0, 0, 150).endVertex();
        bufferbuilder.pos(left, top, 0).tex(0, 0).color(0, 0, 0, 150).endVertex();
        tessellator.draw();

        GlStateManager.shadeModel(7424);
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
    }

    private void renderBar(int left, int right, int top, int bottom) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.disableTexture2D();

        int B = dragging ? 170 : 128;
        int b = dragging ? 252 : 192;

        //actual bar
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(left, bottom, 0).tex(0, 1).color(128, 128, B, 255).endVertex();
        bufferbuilder.pos(right, bottom, 0).tex(1, 1).color(128, 128, B, 255).endVertex();
        bufferbuilder.pos(right, top, 0).tex(1, 0).color(128, 128, B, 255).endVertex();
        bufferbuilder.pos(left, top, 0).tex(0, 0).color(128, 128, B, 255).endVertex();
        tessellator.draw();

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(left, bottom - 1, 0).tex(0, 1).color(192, 192, b, 255).endVertex();
        bufferbuilder.pos(right - 1, bottom - 1, 0).tex(1, 1).color(192, b, 192, 255).endVertex();
        bufferbuilder.pos(right - 1, top, 0).tex(1, 0).color(192, 192, b, 255).endVertex();
        bufferbuilder.pos(left, top, 0.0D).tex(0, 0).color(192, 192, b, 255).endVertex();
        tessellator.draw();

        GlStateManager.enableTexture2D();
    }

    private int computeShiftFactor(int mouseX, int mouseY) {
        int pos = mouseY;

        int scrubberLength = getScrubberLength(elementHeight, contentHeight);
        int scrubberStart = getScrubberStart(scrubberLength, elementHeight, contentHeight);

        if (pos < scrubberStart) {
            return 1;
        } else if (pos > scrubberStart + scrubberLength) {
            return -1;
        }

        return 0;
    }

    public boolean performAction(int mouseX, int mouseY) {

        if (!isFocused(mouseX, mouseY)) {
            touching = true;
            return true;
        }

        shiftFactor = computeShiftFactor(mouseX, mouseY);

        if (shiftFactor == 0) {
            Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1));
            dragging = true;
        }

        return true;
    }

    public void mouseMove(int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            scrollBy(initialMouseY - mouseY);
        } else if (touching) {
            scrollMomentum = mouseY - initialMouseY;

            scrollBy((mouseY - initialMouseY) / 4);
        }

        initialMouseY = mouseY;
    }

    public void mouseUp(int mouseX, int mouseY) {
        dragging = touching = false;
        shiftFactor = 0;
    }

    public void scrollBy(float y) {
        scrollY = MathHelper.clamp((int)Math.floor(scrollY - y * scrollFactor), 0, maxScrollY);
    }

}
