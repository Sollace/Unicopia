package com.minelittlepony.unicopia.client.gui.spellbook;

import java.util.List;

import com.minelittlepony.common.client.gui.IViewRoot;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.client.gui.*;
import com.minelittlepony.unicopia.entity.player.*;
import com.minelittlepony.unicopia.util.ColorHelper;
import com.sollace.romanizer.api.Romanizer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class SpellbookProfilePageContent implements SpellbookChapterList.Content {
    private final MinecraftClient client = MinecraftClient.getInstance();
    private final Pony pony = Pony.of(client.player);
    private final TextRenderer font = client.textRenderer;

    private final SpellbookScreen screen;

    public SpellbookProfilePageContent(SpellbookScreen screen) {
        this.screen = screen;
    }

    @Override
    public void init(SpellbookScreen screen, Identifier pageId) {
        Bounds bounds = screen.getFrameBounds();
        int size = 32;
        int x = screen.getX() + bounds.left + bounds.width / 4 - size + 5;
        int y = screen.getY() + bounds.top + bounds.height / 2 + 3;

        screen.addDrawable(new SpellbookScreen.ImageButton(x, y, size, size))
            .getStyle()
                .setIcon(TribeButton.createSprite(pony.getSpecies(), 0, 0, size))
                .setTooltip(() -> List.of(
                        Text.literal(String.format("Level %d ", pony.getLevel().get() + 1)).append(pony.getSpecies().getDisplayName()).formatted(pony.getSpecies().getAffinity().getColor()),
                        Text.literal(String.format("Mana: %d%%", (int)(pony.getMagicalReserves().getMana().getPercentFill() * 100))),
                        Text.literal(String.format("Corruption: %d%%", (int)(pony.getCorruption().getScaled(100)))),
                        Text.literal(String.format("Experience: %d", (int)(pony.getMagicalReserves().getXp().getPercentFill() * 100))),
                        Text.literal(String.format("Next level in: %dxp", 100 - (int)(pony.getMagicalReserves().getXp().getPercentFill() * 100)))
                ));

        Race inherited = pony.getCompositeRace().collapsed();
        if (inherited != pony.getSpecies()) {
            int halfSize = size / 2;
            screen.addDrawable(new SpellbookScreen.ImageButton(x + halfSize, y + halfSize, halfSize, halfSize))
                .getStyle()
                    .setIcon(TribeButton.createSprite(inherited, 0, 0, halfSize));
        }
    }

    @Override
    public boolean showInventory() {
        return true;
    }

    @Override
    public void draw(DrawContext context, int mouseX, int mouseY, IViewRoot container) {

        int y = SpellbookScreen.TITLE_Y;

        float tickDelta = client.getTickDelta();
        float delta = pony.asEntity().age + tickDelta;
        int currentLevel = pony.getLevel().get();
        float currentScaledLevel = pony.getLevel().getScaled(1);
        float currentCorruption = pony.getCorruption().getScaled(1);

        DrawableUtil.drawScaledText(context, pony.asEntity().getName(), SpellbookScreen.TITLE_X, y, 1.3F, SpellbookScreen.TITLE_COLOR);
        DrawableUtil.drawScaledText(context, ExperienceGroup.forLevel(
                currentScaledLevel,
                currentCorruption
        ), SpellbookScreen.TITLE_X, y + 13, 0.8F,
                ColorHelper.lerp(currentCorruption,
                        ColorHelper.lerp(currentScaledLevel, 0xAA0040FF, 0xAAA0AA40),
                        0xAAFF0000
                )
        );

        MagicReserves reserves = pony.getMagicalReserves();

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(screen.getBackgroundWidth() / 2 + SpellbookScreen.TITLE_X - 10, y, 0);
        matrices.scale(1.3F, 1.3F, 1);
        context.drawText(font, SpellbookCraftingPageContent.INVENTORY_TITLE, 0, 0, SpellbookScreen.TITLE_COLOR, false);
        matrices.pop();

        Bounds bounds = screen.getFrameBounds();

        matrices.push();
        matrices.translate(bounds.left + bounds.width / 4 - 10, bounds.top + bounds.height / 2 + 20, 0);

        double growth = MathHelper.sin(delta / 9F) * 2;

        double radius = 40 + growth;
        float xpPercentage = reserves.getXp().getPercentFill(tickDelta);
        float manaPercentage = reserves.getMana().getPercentFill(tickDelta);

        float alphaF = (MathHelper.sin(delta / 9F) + 1) / 2F;
        int alpha = (int)(alphaF * 0x10) & 0xFF;
        int color = 0x10404000 | alpha;
        int xpColor = 0xAA0040FF | ((int)((0.3F + 0.7F * xpPercentage) * 0xFF) & 0xFF) << 16;
        int manaColor = 0xFF00F040 | (int)((0.3F + 0.7F * alphaF) * 0x40) << 16;

        DrawableUtil.drawArc(matrices, 0, radius + 24, 0, DrawableUtil.TAU, color, false);
        DrawableUtil.drawArc(matrices, radius / 3, radius + 6, 0, DrawableUtil.TAU, color, false);

        if (currentLevel >= pony.getLevel().getMax()) {
            int rayCount = 6;
            float raySeparation = MathHelper.TAU / rayCount;
            float rotate = (delta / 120) % (MathHelper.TAU / (rayCount / 2));

            growth = MathHelper.sin(delta / 10F) * 2;

            int bandAColor = ColorHelper.lerp(currentCorruption, 0xAAFFAA60, 0xFF000030);
            int bandBColor = ColorHelper.lerp(currentCorruption, 0xFFFFFF40, 0x00000020);

            float glowSize = ColorHelper.lerp(currentCorruption, 8, -8);

            for (int i = 0; i < rayCount; i++) {
                double rad = (radius + glowSize) * 0.8F + growth - (i % 2) * 5;
                float rot = (rotate + raySeparation * i) % MathHelper.TAU;

                DrawableUtil.drawArc(matrices, 0, rad, rot, 0.2F, bandAColor, false);
                DrawableUtil.drawArc(matrices, 0, rad + 0.3F, rot + 0.37F, 0.25F, bandBColor, false);
            }
        }

        DrawableUtil.drawArc(matrices, radius / 3, radius + 6, 0, xpPercentage * DrawableUtil.TAU, xpColor, false);
        radius += 8;
        DrawableUtil.drawArc(matrices, radius, radius + 6 + growth, 0, manaPercentage * DrawableUtil.TAU, manaColor, false);

        String manaString = (int)reserves.getMana().get() + "/" + (int)reserves.getMana().getMax();

        y = 15;
        Text manaLabel = Text.translatable("gui.unicopia.spellbook.page.mana");
        context.drawText(font, manaLabel, -font.getWidth(manaLabel) / 2, y, SpellbookScreen.TITLE_COLOR, false);
        context.drawText(font, manaString, -font.getWidth(manaString) / 2, y += font.fontHeight, SpellbookScreen.TITLE_COLOR, false);

        Text levelString = Text.literal(Romanizer.romanize(currentLevel + 1));

        matrices.translate(-font.getWidth(levelString), -35, 0);
        matrices.scale(2F, 2F, 1);
        context.drawText(font, levelString, 0, 0, SpellbookScreen.TITLE_COLOR, false);
        matrices.pop();

        matrices.push();
        matrices.translate(-screen.getX(), -screen.getY(), 0);
        screen.drawSlots(context, mouseX, mouseY, 0);
        matrices.pop();
    }

    static void drawBar(DrawContext context, int x, int y, float value, int color) {
        int barWidth = 40;
        int midpoint = x + (int)(barWidth * value);
        context.fill(x, y, midpoint, y + 5, 0xFFAAFFFF);
        context.fill(midpoint, y, x + barWidth, y + 5, color);
    }
}
