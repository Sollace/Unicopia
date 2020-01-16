package com.minelittlepony.unicopia.inventory.gui;

import java.io.IOException;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.ContainerScreen9;
import net.minecraft.util.Identifier;

public class GuiOfHolding extends ContainerScreen9 {
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("textures/gui/container/generic_54.png");

    private final int inventoryRows;
    private final int playerRows;

    private final Scrollbar scrollbar = new Scrollbar();

    public GuiOfHolding(IInteractionObject interaction) {
        super(interaction.createContainer(MinecraftClient.getInstance().player.inventory, MinecraftClient.getInstance().player));

        playerRows = MinecraftClient.getInstance().player.inventory.getSizeInventory() / 9;
        inventoryRows = (inventorySlots.inventorySlots.size() / 9) - 1;
    }

    @Override
    public void initGui() {

        super.initGui();

        scrollbar.reposition(
                guiLeft + xSize,
                guiTop,
                ySize,
                (inventoryRows + 1) * 18 + 17);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        mc.player.playSound(SoundEvents.BLOCK_ENDERCHEST_OPEN, 0.5F, 0.5F);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        scrollbar.render(mouseX, mouseY, partialTicks);

        int scroll = -scrollbar.getScrollAmount();

        GlStateManager.pushMatrix();
        GlStateManager.translatef(0, scroll, 0);

        super.drawScreen(mouseX, mouseY - scroll, partialTicks);

        int h = height;
        height = Integer.MAX_VALUE;
        renderHoveredToolTip(mouseX, mouseY - scroll);
        height = h;

        GlStateManager.popMatrix();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        scrollbar.performAction(mouseX, mouseY);
        super.mouseClicked(mouseX, mouseY + scrollbar.getScrollAmount(), mouseButton);
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY + scrollbar.getScrollAmount(), clickedMouseButton, timeSinceLastClick);

        if (!dragSplitting) {
            scrollbar.mouseMove(mouseX, mouseY, timeSinceLastClick);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY + scrollbar.getScrollAmount(), state);
        scrollbar.mouseUp(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        ContainerOfHolding coh = (ContainerOfHolding)inventorySlots;

        fontRenderer.drawString(coh.getName(), 8, 6, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1, 1, 1, 1);

        mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);

        int midX = (width - xSize) / 2;
        int midY = (height - ySize) / 2;

        drawTexturedModalRect(midX, midY, 0, 0, xSize, 18);
        for (int i = 0; i < inventoryRows - (playerRows - 1); i++) {
            drawTexturedModalRect(midX, midY + (18 * (i + 1)), 0, 18, xSize, 18);
        }

        drawTexturedModalRect(midX, midY + (18 * (inventoryRows - (playerRows - 2))) - 1, 0, 131, xSize, 98);
    }
}
