package com.minelittlepony.unicopia.container;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.entity.player.*;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class SpellbookProfilePageContent extends DrawableHelper implements SpellbookChapterList.Content {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Pony pony = Pony.of(client.player);
    private final TextRenderer font = client.textRenderer;

    private final SpellbookScreen screen;

    public SpellbookProfilePageContent(SpellbookScreen screen) {
        this.screen = screen;
    }

    @Override
    public void init(SpellbookScreen screen) {

    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, IViewRoot container) {

        int y = SpellbookScreen.TITLE_Y;

        float delta = pony.getEntity().age + client.getTickDelta();
        int currentLevel = pony.getLevel().get();

        DrawableUtil.drawScaledText(matrices, pony.getEntity().getName(), SpellbookScreen.TITLE_X, y, 1.3F, SpellbookScreen.TITLE_COLOR);
        DrawableUtil.drawScaledText(matrices, ExperienceGroup.forLevel(currentLevel).getLabel(), SpellbookScreen.TITLE_X, y + 13, 0.8F, 0xAA0040FF);

        MagicReserves reserves = pony.getMagicalReserves();

        matrices.push();
        matrices.translate(screen.getBackgroundWidth() / 2 + SpellbookScreen.TITLE_X - 10, y, 0);
        matrices.scale(1.3F, 1.3F, 1);
        font.draw(matrices, SpellbookPage.INVENTORY.getLabel(), 0, 0, SpellbookScreen.TITLE_COLOR);
        matrices.pop();

        Bounds bounds = screen.getFrameBounds();

        matrices.push();
        matrices.translate(bounds.left + bounds.width / 4 - 10, bounds.top + bounds.height / 2 + 20, 0);

        double growth = MathHelper.sin(delta / 9F) * 2;

        double radius = 40 + growth;
        float xpPercentage = reserves.getXp().getPercentFill();
        float manaPercentage = reserves.getMana().getPercentFill();

        float alphaF = (MathHelper.sin(delta / 9F) + 1) / 2F;
        int alpha = (int)(alphaF * 0x10) & 0xFF;
        int color = 0x10404000 | alpha;
        int xpColor = 0xAA0040FF | ((int)((0.3F + 0.7F * xpPercentage) * 0xFF) & 0xFF) << 16;
        int manaColor = 0xFF00F040 | (int)((0.3F + 0.7F * alphaF) * 0x40) << 16;

        DrawableUtil.drawArc(matrices, 0, radius + 24, 0, DrawableUtil.TAU, color, false);
        DrawableUtil.drawArc(matrices, radius / 3, radius + 6, 0, DrawableUtil.TAU, color, false);
        DrawableUtil.drawArc(matrices, radius / 3, radius + 6, 0, xpPercentage * DrawableUtil.TAU, xpColor, false);
        radius += 8;
        DrawableUtil.drawArc(matrices, radius, radius + 6 + growth, 0, manaPercentage * DrawableUtil.TAU, manaColor, false);

        String manaString = (int)reserves.getMana().get() + "/" + (int)reserves.getMana().getMax();

        y = 15;
        font.draw(matrices, "Mana", -font.getWidth("Mana") / 2, y, SpellbookScreen.TITLE_COLOR);
        font.draw(matrices, manaString, -font.getWidth(manaString) / 2, y += font.fontHeight, SpellbookScreen.TITLE_COLOR);

        matrices.push();
        matrices.scale(1.4F, 1.4F, 1);
        RenderSystem.setShaderTexture(0, Unicopia.id("textures/gui/icons.png"));
        drawTexture(matrices, -8, -8, 16 * 2, 0, 16, 16, 256, 256);
        RenderSystem.setShaderTexture(0, SpellbookScreen.TEXTURE);
        matrices.pop();

        Text levelString = I18n.hasTranslation("enchantment.level." + (currentLevel + 1)) ? Text.translatable("enchantment.level." + (currentLevel + 1)) : Text.literal(currentLevel >= 999 ? ">999" : "" + (currentLevel + 1));

        matrices.translate(-font.getWidth(levelString), -35, 0);
        matrices.scale(2F, 2F, 1);
        font.draw(matrices, levelString, 0, 0, SpellbookScreen.TITLE_COLOR);
        matrices.pop();

        matrices.push();
        matrices.translate(-screen.getX(), -screen.getY(), 0);
        screen.drawSlots(matrices, mouseX, mouseY, 0);
        matrices.pop();
    }

    static void drawBar(MatrixStack matrices, int x, int y, float value, int color) {
        int barWidth = 40;
        int midpoint = x + (int)(barWidth * value);
        fill(matrices, x, y, midpoint, y + 5, 0xFFAAFFFF);
        fill(matrices, midpoint, y, x + barWidth, y + 5, color);
    }
}
