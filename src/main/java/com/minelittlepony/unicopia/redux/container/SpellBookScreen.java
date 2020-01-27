package com.minelittlepony.unicopia.redux.container;

import java.io.IOException;

import org.lwjgl.opengl.GL11;

import com.minelittlepony.unicopia.core.SpeciesList;
import com.minelittlepony.unicopia.core.UnicopiaCore;
import com.minelittlepony.unicopia.core.enchanting.IPage;
import com.minelittlepony.unicopia.core.enchanting.PageState;
import com.minelittlepony.unicopia.core.entity.player.IPlayer;
import com.minelittlepony.unicopia.redux.enchanting.IPageUnlockListener;
import com.minelittlepony.unicopia.redux.enchanting.Pages;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Identifier;

public class SpellBookScreen extends ContainerScreen implements IPageUnlockListener {

    private static IPage currentIPage;

    public static final Identifier spellBookGuiTextures = new Identifier("unicopia", "textures/gui/container/book.png");

    private IPlayer playerExtension;

    private PageButton nextPage;
    private PageButton prevPage;

    public SpellBookScreen(PlayerEntity player) {
        super(new SpellBookContainer(player.inventory, player.world, new BlockPos(player)));
        player.openContainer = inventorySlots;

        xSize = 405;
        ySize = 219;
        allowUserInput = true;
        playerExtension = SpeciesList.instance().getPlayer(player);
    }

    @Override
    public void initGui() {
        super.initGui();
        buttonList.clear();

        int x = (width - xSize) / 2;
        int y = (height - ySize) / 2;

        buttonList.add(nextPage = new PageButton(1, x + 360, y + 185, true));
        buttonList.add(prevPage = new PageButton(2, x + 20, y + 185, false));

        if (currentIPage == null) {
            currentIPage = Pages.instance().getByIndex(0);
        }

        onPageChange();

        if (playerExtension.hasPageStateRelative(currentIPage, PageState.UNREAD, 1)) {
            nextPage.triggerShake();
        }

        if (playerExtension.hasPageStateRelative(currentIPage, PageState.UNREAD, -1)) {
            prevPage.triggerShake();
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 1) {
            currentIPage = currentIPage.next();
        } else {
            currentIPage = currentIPage.prev();
        }

        onPageChange();
    }

    protected void onPageChange() {
        prevPage.enabled = currentIPage.getIndex() > 0;
        nextPage.enabled = currentIPage.getIndex() < Pages.instance().getTotalPages() - 1;

        if (playerExtension.getPageState(currentIPage) == PageState.UNREAD) {
            playerExtension.setPageState(currentIPage, PageState.READ);
        }
    }

    @Override
    public boolean onPageUnlocked(IPage page) {
        int i = currentIPage.compareTo(page);

        if (i <= 0) {
            prevPage.triggerShake();
        }

        if (i >= 0) {
            nextPage.triggerShake();
        }

        return true;
    }

    @Override
    protected void drawGradientRect(int left, int top, int width, int height, int startColor, int endColor) {
        Slot slot = getSlotUnderMouse();

        if (slot == null || left != slot.xPos || top != slot.yPos || !drawSlotOverlay(slot)) {
            super.drawGradientRect(left, top, width, height, startColor, endColor);
        }
    }

    protected boolean drawSlotOverlay(Slot slot) {
        if (slot instanceof SpellbookSlot) {
            GlStateManager.enableBlend();
            GL11.glDisable(GL11.GL_ALPHA_TEST);

            mc.getTextureManager().bindTexture(spellBookGuiTextures);
            drawModalRectWithCustomSizedTexture(slot.xPos - 1, slot.yPos - 1, 74, 223, 18, 18, 512, 256);

            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GlStateManager.disableBlend();

            return true;
        }

        return false;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String text = String.format("%d / %d", currentIPage.getIndex() + 1, Pages.instance().getTotalPages());

        fontRenderer.drawString(text, 70 - fontRenderer.getStringWidth(text)/2, 190, 0x0);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        drawWorldBackground(0);
        GlStateManager.color(1, 1, 1, 1);

        int left = (width - xSize) / 2;
        int top = (height - ySize) / 2;

        mc.getTextureManager().bindTexture(spellBookGuiTextures);
        drawModalRectWithCustomSizedTexture(left, top, 0, 0, xSize, ySize, 512, 256);

        GlStateManager.enableBlend();
        GL11.glDisable(GL11.GL_ALPHA_TEST);

        mc.getTextureManager().bindTexture(spellBookGuiTextures);
        drawModalRectWithCustomSizedTexture(left + 147, top + 49, 407, 2, 100, 101, 512, 256);

        if (playerExtension.getPageState(currentIPage) != PageState.LOCKED) {
            Identifier texture = currentIPage.getTexture();

            if (mc.getTextureManager().getTexture(texture) != TextureUtil.MISSING_TEXTURE) {
                GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                mc.getTextureManager().bindTexture(texture);
                drawModalRectWithCustomSizedTexture(left, top, 0, 0, xSize, ySize, 512, 256);
            } else {
                if (playerExtension.getWorld().rand.nextInt(100) == 0) {
                    UnicopiaCore.LOGGER.fatal("Missing texture " + texture);
                }
            }
        }

        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GlStateManager.disableBlend();
    }

    static class PageButton extends GuiButton {
        private final boolean direction;

        private int shakesLeft = 0;
        private float shakeCount = 0;

        public PageButton(int id, int x, int y, boolean direction) {
            super(id, x, y, 23, 13, "");
            this.direction = direction;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            if (visible) {

                boolean shaking = false;

                int x = this.x;
                int y = this.y;

                if (shakesLeft > 0) {
                    shaking = true;
                    shakeCount += (float)Math.PI / 2;

                    if (shakeCount >= Math.PI * 2) {
                        shakeCount %= Math.PI * 2;
                        shakesLeft--;
                    }

                    x += (int)(Math.sin(shakeCount) * 3);
                    y -= (int)(Math.sin(shakeCount) * 3);
                }

                GlStateManager.color(1, 1, 1, 1);
                mc.getTextureManager().bindTexture(spellBookGuiTextures);

                int u = 0;
                int v = 220;

                if (shaking || isMouseOver(mouseX, mouseY)) {
                    u += 23;
                }

                if (shaking) {
                    u += 23;
                }

                if (!direction) {
                    v += 13;
                }

                drawModalRectWithCustomSizedTexture(x, y, u, v, 23, 13, 512, 256);
            }
        }

        public boolean isMouseOver(int mouseX, int mouseY) {
            return enabled &&  mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;

        }

        public void triggerShake() {
            if (shakesLeft <= 0) {
                shakesLeft = 5;
            }
        }
    }
}
