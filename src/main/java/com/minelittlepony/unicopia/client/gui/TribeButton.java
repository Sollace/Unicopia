package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.common.client.gui.element.Button;
import com.minelittlepony.common.client.gui.sprite.TextureSprite;
import com.minelittlepony.unicopia.Race;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class TribeButton extends Button {

    private final int screenWidth;
    private final Race race;

    public TribeButton(int x, int y, int screenWidth, Race race) {
        super(x, y, 70, 70);
        this.screenWidth = screenWidth;
        this.race = race;
        int size = 32;
        int textureSize = 512;
        int ordinal = Race.REGISTRY.getRawId(race);

        getStyle()
            .setIcon(new TextureSprite()
                .setPosition((70 - size) / 2, 0)
                .setSize(size, size)
                .setTextureSize(textureSize, textureSize)
                .setTexture(TribeSelectionScreen.ICONS)
                .setTextureOffset((size * ordinal) % textureSize, (ordinal / textureSize) * size)
            )
            .setText(race.getTranslationKey());
    }

    @Override
    public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float partialTicks) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TribeSelectionScreen.TEXTURE);
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(
                GlStateManager.SrcFactor.SRC_ALPHA,
                GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        );

        MinecraftClient mc = MinecraftClient.getInstance();

        drawTexture(matrices, x  - 3, y - 13, 0, 0, 76, 69);
        if (isHovered()) {
            drawTexture(matrices, x  - 4, y - 14, 76, 0, 78, 71);

            if (hovered && screenWidth > 0) {
                Identifier id = Race.REGISTRY.getId(race);
                drawCenteredText(matrices, getFont(), new TranslatableText("gui.unicopia.tribe_selection.describe." + id.getNamespace() + "." + id.getPath()), screenWidth / 2, y + height, 0xFFFFFFFF);
            }
        }

        if (getStyle().hasIcon()) {
            getStyle().getIcon().render(matrices, x, y, mouseX, mouseY, partialTicks);
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
}