package com.minelittlepony.unicopia.client.gui;

import org.joml.Matrix4f;

import com.minelittlepony.unicopia.Race;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public interface DrawableUtil {
    double PI = Math.PI;
    double TAU = Math.PI * 2;
    double NUM_RINGS = 300;
    double INCREMENT = TAU / NUM_RINGS;

    static void drawScaledText(DrawContext context, Text text, int x, int y, float size, int color) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(size, size, 1);
        context.drawText(MinecraftClient.getInstance().textRenderer, text, 0, 0, color, false);
        matrices.pop();
    }

    static void renderItemIcon(DrawContext context ,ItemStack stack, double x, double y, float scale) {
        MatrixStack modelStack = context.getMatrices();
        modelStack.push();
        modelStack.translate(x, y, 0);
        if (scale != 1) {
            modelStack.scale(scale, scale, 1);
        }
        context.drawItem(stack, 0, 0);

        modelStack.pop();
    }

    static void renderRaceIcon(DrawContext context, Race race, int x, int y, int size) {
        context.drawTexture(RenderLayer::getGuiTextured, race.getIcon(), x - size / 2, y - size / 2, 0, 0, 0, size, size, size, size);
    }

    static void drawLine(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float r = ColorHelper.getAlpha(color);
        float g = ColorHelper.getRed(color);
        float b = ColorHelper.getGreen(color);
        float k = ColorHelper.getBlue(color);
        Immediate vertices = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer bufferBuilder = vertices.getBuffer(RenderLayer.getDebugLineStrip(3));
        bufferBuilder.vertex(matrix, x1, y1, 0).color(r, g, b, k);
        bufferBuilder.vertex(matrix, x2, y2, 0).color(r, g, b, k);
        vertices.draw();
    }

    /**
     * Renders a colored arc with notches.
     *
     * @param mirrorHorizontally Whether or not the arc must be mirrored across the horizontal plane. Will produce a bar that grows from the middle filling both sides.
     */
    static void drawNotchedArc(MatrixStack matrices, double innerRadius, double outerRadius, double startAngle, double arcAngle, double notchAngle, double notchSpacing, int color) {
        double notchBegin = startAngle;
        double endAngle = startAngle + arcAngle;
        while (notchBegin < endAngle) {
            double notchEnd = Math.min(notchBegin + notchAngle, endAngle);
            if (notchEnd <= notchBegin) {
                return;
            }
            drawArc(matrices, innerRadius, outerRadius, notchBegin, notchEnd - notchBegin, color);
            notchBegin += notchAngle + notchSpacing;
        }

    }

    /**
     * Renders a colored arc.
     */
    static void drawArc(MatrixStack matrices, double innerRadius, double outerRadius, double startAngle, double arcAngle, int color) {
        if (Math.abs(arcAngle) < INCREMENT) {
            return;
        }

        float r = ColorHelper.getAlpha(color);
        float g = ColorHelper.getRed(color);
        float b = ColorHelper.getGreen(color);
        float k = ColorHelper.getBlue(color);

        arcAngle = Math.min(arcAngle, TAU - INCREMENT);

        final double maxAngle = startAngle + arcAngle;

        Matrix4f model = matrices.peek().getPositionMatrix();

        Immediate vertices = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer bufferBuilder = vertices.getBuffer(RenderLayer.getTranslucent());

        boolean shouldDraw = false;

        for (double angle = -startAngle; angle >= -maxAngle; angle -= INCREMENT) {
            shouldDraw = true;
            // center
            cylendricalVertex(bufferBuilder, model, innerRadius, angle, r, g, b, k);
            // point one
            cylendricalVertex(bufferBuilder, model, outerRadius, angle, r, g, b, k);
            // point two
            cylendricalVertex(bufferBuilder, model, outerRadius, angle + INCREMENT, r, g, b, k);
            // back to center
            cylendricalVertex(bufferBuilder, model, innerRadius, angle + INCREMENT, r, g, b, k);
        }

        if (shouldDraw) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            vertices.draw();
        }
    }

    private static void cylendricalVertex(VertexConsumer bufferBuilder, Matrix4f model, double radius, double angle, float r, float g, float b, float k) {
        bufferBuilder.vertex(model,
                (float)(radius * MathHelper.sin((float)angle)),
                (float)(radius * MathHelper.cos((float)angle)), 0).color(r, g, b, k).normal(2, 2, 2);
    }
}
