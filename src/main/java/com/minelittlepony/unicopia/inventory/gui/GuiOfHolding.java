package com.minelittlepony.unicopia.inventory.gui;

import java.io.IOException;

import com.minelittlepony.unicopia.inventory.ContainerOfHolding;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IInteractionObject;

public class GuiOfHolding extends GuiContainer {
    private static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");

    private final int inventoryRows;

    private final Scrollbar scrollbar = new Scrollbar();

    public GuiOfHolding(IInteractionObject interaction) {
        super(interaction.createContainer(Minecraft.getMinecraft().player.inventory, Minecraft.getMinecraft().player));

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
        GlStateManager.translate(0, scroll, 0);

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

        fontRenderer.drawString(coh.getDisplayName().getUnformattedText(), 8, 6, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);

        mc.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);

        int midX = (width - xSize) / 2;
        int midY = (height - ySize) / 2;

        drawTexturedModalRect(midX, midY, 0, 0, xSize, 18);
        for (int i = 0; i < inventoryRows; i++) {
            drawTexturedModalRect(midX, midY + (18 * (i + 1)), 0, 18, xSize, 18);
        }
        drawTexturedModalRect(midX, midY + inventoryRows * 18 + 17, 0, 193, xSize, 30);
    }
}
