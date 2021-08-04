package com.minelittlepony.unicopia.client.gui;

import com.minelittlepony.unicopia.ability.AbilityDispatcher;
import com.minelittlepony.unicopia.ability.AbilitySlot;
import com.minelittlepony.unicopia.client.KeyBindingsHandler;
import com.minelittlepony.unicopia.entity.player.MagicReserves;
import com.minelittlepony.unicopia.entity.player.Pony;
import com.minelittlepony.unicopia.entity.player.MagicReserves.Bar;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

class ManaRingSlot extends Slot {
    private static final double TWO_PI = Math.PI * 2;

    public ManaRingSlot(UHud uHud, AbilitySlot normalSlot, AbilitySlot backupSlot, int x, int y) {
        super(uHud, normalSlot, backupSlot, x, y, 8, UHud.PRIMARY_SLOT_SIZE, 33, 43, 42);
        background(0, 5);
        foreground(0, 59);
    }

    @Override
    protected void renderContents(MatrixStack matrices, AbilityDispatcher abilities, boolean bSwap, float tickDelta) {
        matrices.push();
        matrices.translate(24.5, 25.5, 0);

        Pony pony = Pony.of(uHud.client.player);
        MagicReserves mana = pony.getMagicalReserves();

        double arcBegin = 0;

        arcBegin = renderRing(matrices, 17, 13, 0, mana.getMana(), 0xFF88FF99, tickDelta);

        if (!uHud.client.player.isCreative()) {
            renderRing(matrices, 13, 11, 0, mana.getXp(), 0x88880099, tickDelta);

            double cost = abilities.getStats().stream()
                    .mapToDouble(s -> s.getCost(KeyBindingsHandler.INSTANCE.page))
                    .reduce(Double::sum)
                    .getAsDouble();

            if (cost > 0) {
                float percent = mana.getMana().getPercentFill();
                float max = mana.getMana().getMax();

                cost *= 10;
                cost /= 1 + pony.getLevel().get();

                int color = cost / max > percent ? 0xFF000099 : 0xFFFF0099;

                cost = Math.min(percent, Math.min(max, cost) / max);

                double angle = cost * Math.PI * 2;

                renderArc(matrices, 13, 17, arcBegin - angle, angle, color, false);
            }
        }

        arcBegin = renderRing(matrices, 17, 13, arcBegin, mana.getEnergy(), 0xFF002299, tickDelta);

        matrices.pop();

        super.renderContents(matrices, abilities, bSwap, tickDelta);
    }

    private double renderRing(MatrixStack matrices, double outerRadius, double innerRadius, double offsetAngle, Bar bar, int color, float tickDelta) {
        double fill = bar.getPercentFill() * TWO_PI;
        double shadow = bar.getShadowFill() * TWO_PI;

        renderArc(matrices, innerRadius, outerRadius, offsetAngle, fill, color, true);

        if (shadow > fill) {
            color = (color & 0xFFFFFF00)
                 | ((color & 0x000000FF) / 2);

            renderArc(matrices, innerRadius, outerRadius, offsetAngle + fill, shadow - fill, color, false);
        }
        return offsetAngle + fill;
    }

    /**
     * Renders a colored arc.
     *
     * @param mirrorHorizontally Whether or not the arc must be mirrored across the horizontal plane. Will produce a bar that grows from the middle filling both sides.
     */
    static void renderArc(MatrixStack matrices, double innerRadius, double outerRadius, double startAngle, double arcAngle, int color, boolean mirrorHorizontally) {
        float r = (color >> 24 & 255) / 255F;
        float g = (color >> 16 & 255) / 255F;
        float b = (color >> 8 & 255) / 255F;
        float k = (color & 255) / 255F;

        final double num_rings = 300;

        final double increment = TWO_PI / num_rings;

        if (arcAngle < increment) {
            return;
        }

        final double maxAngle = MathHelper.clamp(startAngle + arcAngle, 0, TWO_PI - increment);

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();

        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        Matrix4f model = matrices.peek().getModel();

        if (!mirrorHorizontally) {
            startAngle = -startAngle;
        }

        for (double angle = startAngle; angle >= -maxAngle; angle -= increment) {
            // center
            bufferBuilder.vertex(model,
                    (float)(innerRadius * Math.sin(angle)),
                    (float)(innerRadius * Math.cos(angle)), 0).color(r, g, b, k).next();

            // point one
            bufferBuilder.vertex(model,
                    (float)(outerRadius * Math.sin(angle)),
                    (float)(outerRadius * Math.cos(angle)), 0).color(r, g, b, k).next();

            // point two
            bufferBuilder.vertex(model,
                    (float)(outerRadius * Math.sin(angle + increment)),
                    (float)(outerRadius * Math.cos(angle + increment)), 0).color(r, g, b, k).next();

            // back to center
            bufferBuilder.vertex(model,
                    (float)(innerRadius * Math.sin(angle + increment)),
                    (float)(innerRadius * Math.cos(angle + increment)), 0).color(r, g, b, k).next();

        }

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);

        RenderSystem.enableTexture();
    }
}
