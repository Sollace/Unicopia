package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.sprite.ISprite;
import com.minelittlepony.common.client.gui.sprite.TextureSprite;
import com.minelittlepony.unicopia.Race;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TribeButton extends Button {

    private final int screenWidth;
    private final Race race;

    public TribeButton(int x, int y, int screenWidth, Race race) {
        super(x, y, 70, 70);
        this.screenWidth = screenWidth;
        this.race = race;

        getStyle().setIcon(createSprite(race, (70 - 32) / 2, 0, 32)).setText(race.getTranslationKey());
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderTexture(0, TribeSelectionScreen.TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        );

        MinecraftClient mc = MinecraftClient.getInstance();

        drawTexture(matrices, getX()  - 3, getY() - 13, 0, 0, 76, 69);
        if (isHovered()) {
            drawTexture(matrices, getX()  - 4, getY() - 14, 76, 0, 78, 71);

            if (hovered && screenWidth > 0) {
                Identifier id = Race.REGISTRY.getId(race);
                drawCenteredTextWithShadow(matrices, getFont(), Text.translatable("gui.unicopia.tribe_selection.describe." + id.getNamespace() + "." + id.getPath()), screenWidth / 2, getY() + height, 0xFFFFFFFF);
            }
        }

        if (getStyle().hasIcon()) {
            getStyle().getIcon().render(matrices, getX(), getY(), mouseX, mouseY, partialTicks);
        }

        int foreColor = getStyle().getColor();
        if (!active) {
            foreColor = 10526880;
        } else if (isHovered()) {
            foreColor = 16777120;
        }

        setMessage(getStyle().getText());


        renderForground(matrices, mc, mouseX, mouseY, foreColor | MathHelper.ceil(alpha * 255.0F) << 24);
    }

    public static ISprite createSprite(Race race, int x, int y, int size) {
        return new TextureSprite()
            .setPosition(x, y)
            .setSize(size, size)
            .setTextureSize(size, size)
            .setTexture(race.getIcon());
    }
}