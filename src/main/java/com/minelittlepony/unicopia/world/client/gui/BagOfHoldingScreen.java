package com.minelittlepony.unicopia.world.client.gui;

import java.util.List;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.dimension.Padding;
import com.minelittlepony.common.client.gui.element.Scrollbar;
import com.minelittlepony.unicopia.world.container.BagOfHoldingContainer;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BagOfHoldingScreen extends HandledScreen<BagOfHoldingContainer> implements IViewRoot {
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("textures/gui/container/generic_54.png");

    private final int inventoryRows;
    private final int playerRows;

    private final Bounds bounds = Bounds.empty();
    private final Bounds contentBounds = Bounds.empty();
    private final Padding padding = new Padding(0, 0, 0, 0);

    private final Scrollbar scrollbar = new Scrollbar(this);

    public BagOfHoldingScreen(BagOfHoldingContainer handler, PlayerInventory inv, Text title) {
        super(handler, inv, title);

        playerRows = playerInventory.size() / 9;
        inventoryRows = (handler.slots.size() / 9) - 1;
    }

    @Override
    public void init() {
        super.init();

        bounds.left = x;
        bounds.top = y;
        bounds.width = backgroundWidth;
        bounds.height = backgroundHeight;
        contentBounds.width = bounds.width;
        contentBounds.height = (inventoryRows + 1) * 18 + 17;

        scrollbar.reposition();
        children.add(scrollbar);
    }

    @Override
    public void onClose() {
        super.onClose();
        client.player.playSound(SoundEvents.BLOCK_ENDER_CHEST_OPEN, 0.5F, 0.5F);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        renderBackground(matrices);

        scrollbar.render(matrices, mouseX, mouseY, partialTicks);

        int scroll = -scrollbar.getVerticalScrollAmount();

        GlStateManager.pushMatrix();
        GlStateManager.translatef(0, scroll, 0);

        super.render(matrices, mouseX, mouseY - scroll, partialTicks);

        int h = height;
        height = Integer.MAX_VALUE;
        drawMouseoverTooltip(matrices, mouseX, mouseY - scroll);
        height = h;

        GlStateManager.popMatrix();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return super.mouseClicked(x, y + scrollbar.getVerticalScrollAmount(), button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {

        if (scrollbar.isMouseOver(x, y)) {
            return scrollbar.mouseDragged(x, y + scrollbar.getVerticalScrollAmount(), button, dx, dy);
        }

        return super.mouseDragged(x, y + scrollbar.getVerticalScrollAmount(), button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return super.mouseReleased(x, y + scrollbar.getVerticalScrollAmount(), button);
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {
        textRenderer.drawWithShadow(matrices, title, 8, 6, 0x404040);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1, 1, 1, 1);

        client.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);

        int midX = (width - backgroundWidth) / 2;
        int midY = (height - backgroundHeight) / 2;

        drawTexture(matrices, midX, midY, 0, 0, backgroundWidth, 18);
        for (int i = 0; i < inventoryRows - (playerRows - 1); i++) {
            drawTexture(matrices, midX, midY + (18 * (i + 1)), 0, 18, backgroundWidth, 18);
        }

        drawTexture(matrices, midX, midY + (18 * (inventoryRows - (playerRows - 2))) - 1, 0, 131, backgroundWidth, 98);
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void setBounds(Bounds bounds) {
    }

    @Override
    public Bounds getContentBounds() {
        return contentBounds;
    }

    @Override
    public Padding getContentPadding() {
        return padding;
    }

    @Override
    public List<Element> getChildElements() {
        // TODO Auto-generated method stub
        return children;
    }
}
