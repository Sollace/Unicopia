package com.minelittlepony.unicopia.container;

import com.minelittlepony.common.client.gui.element.Scrollbar;
import com.minelittlepony.unicopia.item.BagOfHoldingItem;
import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.gui.screen.ingame.AbstractContainerScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class BagOfHoldingScreen extends AbstractContainerScreen<BagOfHoldingContainer> {
    private static final Identifier CHEST_GUI_TEXTURE = new Identifier("textures/gui/container/generic_54.png");

    private final int inventoryRows;
    private final int playerRows;

    private final Scrollbar scrollbar = new Scrollbar();

    public BagOfHoldingScreen(PlayerEntity player, BagOfHoldingItem.ContainerProvider provider) {
        super(provider.createMenu(0, player.inventory, player), player.inventory, provider.getDisplayName());

        playerRows = playerInventory.getInvSize() / 9;
        inventoryRows = (container.slotList.size() / 9) - 1;
    }

    @Override
    public void init() {
        super.init();
        scrollbar.reposition(
                left + containerWidth,
                top,
                containerHeight,
                (inventoryRows + 1) * 18 + 17);
        children.add(scrollbar);
    }

    @Override
    public void onClose() {
        super.onClose();
        minecraft.player.playSound(SoundEvents.BLOCK_ENDER_CHEST_OPEN, 0.5F, 0.5F);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        renderBackground();

        scrollbar.render(mouseX, mouseY, partialTicks);

        int scroll = -scrollbar.getScrollAmount();

        GlStateManager.pushMatrix();
        GlStateManager.translatef(0, scroll, 0);

        super.render(mouseX, mouseY - scroll, partialTicks);

        int h = height;
        height = Integer.MAX_VALUE;
        drawMouseoverTooltip(mouseX, mouseY - scroll);
        height = h;

        GlStateManager.popMatrix();
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        return super.mouseClicked(x, y + scrollbar.getScrollAmount(), button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return super.mouseDragged(x, y + scrollbar.getScrollAmount(), button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        return super.mouseReleased(x, y + scrollbar.getScrollAmount(), button);
    }

    @Override
    protected void drawForeground(int mouseX, int mouseY) {
        font.draw(title.asString(), 8, 6, 0x404040);
    }

    @Override
    protected void drawBackground(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1, 1, 1, 1);

        minecraft.getTextureManager().bindTexture(CHEST_GUI_TEXTURE);

        int midX = (width - containerWidth) / 2;
        int midY = (height - containerHeight) / 2;

        blit(midX, midY, 0, 0, containerWidth, 18);
        for (int i = 0; i < inventoryRows - (playerRows - 1); i++) {
            blit(midX, midY + (18 * (i + 1)), 0, 18, containerWidth, 18);
        }

        blit(midX, midY + (18 * (inventoryRows - (playerRows - 2))) - 1, 0, 131, containerWidth, 98);
    }
}
