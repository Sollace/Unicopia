package com.minelittlepony.unicopia.container;

import com.minelittlepony.unicopia.container.SpellbookScreenHandler.SpellbookSlot;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SpellbookScreen extends HandledScreen<SpellbookScreenHandler> {
    public static final Identifier TEXTURE = new Identifier("unicopia", "textures/gui/container/book.png");

    public SpellbookScreen(SpellbookScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
        backgroundWidth = 405;
        backgroundHeight = 219;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        super.render(matrices, mouseX, mouseY, partialTicks);
        drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        renderBackground(matrices, 0);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        int left = (width - backgroundWidth) / 2;
        int top = (height - backgroundHeight) / 2;

        RenderSystem.setShaderTexture(0, TEXTURE);

        drawTexture(matrices, left, top, 0, 0, backgroundWidth, backgroundHeight, 512, 256);

        matrices.push();
        matrices.translate(this.x, this.y, 0);

        RenderSystem.enableBlend();
        for (Slot slot : handler.slots) {
            if (slot.isEnabled() && slot instanceof SpellbookSlot) {
                drawTexture(matrices, slot.x - 1, slot.y - 1, 74, 223, 18, 18, 512, 256);
              //  drawStringWithShadow(matrices, this.textRenderer, ((SpellbookSlot)slot).getRing() + "", slot.x, slot.y, 0x000000FF);
            }
        }
        RenderSystem.disableBlend();

        RenderSystem.enableBlend();
        drawTexture(matrices, 56, 50, 407, 2, 100, 101, 512, 256);
        RenderSystem.disableBlend();

        matrices.pop();
    }
}
