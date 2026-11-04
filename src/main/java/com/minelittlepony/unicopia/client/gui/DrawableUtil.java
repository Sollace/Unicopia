package com.minelittlepony.unicopia.client.gui;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import com.minelittlepony.unicopia.Race;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
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

    static void drawScaledText(DrawContext context, String text, int x, int y, float size, int color) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(size, size, 1);
        context.drawText(MinecraftClient.getInstance().textRenderer, text, 0, 0, color, false);
        matrices.pop();
    }

    static void renderItemIcon(DrawContext context, ItemStack stack, double x, double y, float scale) {
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
        Immediate vertices = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer bufferBuilder = vertices.getBuffer(RenderLayer.getDebugLineStrip(3));
        bufferBuilder.vertex(matrix, x1, y1, 0).color(color);
        bufferBuilder.vertex(matrix, x2, y2, 0).color(color);
        vertices.draw();
    }

    /**
     * Renders a colored arc with notches.
     *
     * @param mirrorHorizontally Whether or not the arc must be mirrored across the horizontal plane. Will produce a bar that grows from the middle filling both sides.
     */
    static void drawNotchedArc(DrawContext context, double innerRadius, double outerRadius, double startAngle, double arcAngle, double notchAngle, double notchSpacing, int color) {
        double notchBegin = startAngle;
        double endAngle = startAngle + arcAngle;
        while (notchBegin < endAngle) {
            double notchEnd = Math.min(notchBegin + notchAngle, endAngle);
            if (notchEnd <= notchBegin) {
                return;
            }
            drawArc(context, innerRadius, outerRadius, notchBegin, notchEnd - notchBegin, color);
            notchBegin += notchAngle + notchSpacing;
        }
    }

    /**
     * Renders a colored arc.
     */
    static void drawArc(DrawContext context, double innerRadius, double outerRadius, double startAngle, double arcAngle, int color) {
        if (Math.abs(arcAngle) < INCREMENT) {
            return;
        }

        arcAngle = Math.min(arcAngle, TAU - INCREMENT);

        final double maxAngle = startAngle + arcAngle;

        Matrix4f model = context.getMatrices().peek().getPositionMatrix();
        context.draw(vertices -> {
            VertexConsumer bufferBuilder = vertices.getBuffer(RenderLayer.getTranslucent());
            for (double angle = -startAngle; angle >= -maxAngle; angle -= INCREMENT) {
                // center
                cylendricalVertex(bufferBuilder, model, innerRadius, angle, color);
                // point one
                cylendricalVertex(bufferBuilder, model, outerRadius, angle, color);
                // point two
                cylendricalVertex(bufferBuilder, model, outerRadius, angle + INCREMENT, color);
                // back to center
                cylendricalVertex(bufferBuilder, model, innerRadius, angle + INCREMENT, color);
            }
        });
    }

    private static void cylendricalVertex(VertexConsumer bufferBuilder, Matrix4f matrix, double radius, double angle, int color) {
        Vector3f pos = matrix.transformPosition(
                (float)(radius * MathHelper.sin((float)angle)),
                (float)(radius * MathHelper.cos((float)angle)),
                0, new Vector3f());
        bufferBuilder.vertex(pos.x, pos.y, pos.z, color, 0, 0, OverlayTexture.DEFAULT_UV, LightmapTextureManager.MAX_LIGHT_COORDINATE, 2, 2, 2);
    }
}
