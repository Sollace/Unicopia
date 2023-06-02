package com.minelittlepony.unicopia.client.gui;

import org.joml.Matrix4f;

import com.minelittlepony.unicopia.Race;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public interface DrawableUtil {
    double PI = Math.PI;
    double TAU = Math.PI * 2;
    double NUM_RINGS = 300;
    double INCREMENT = TAU / NUM_RINGS;

    static void drawScaledText(MatrixStack matrices, Text text, int x, int y, float size, int color) {
        matrices.push();
        matrices.translate(x, y, 0);
        matrices.scale(size, size, 1);
        MinecraftClient.getInstance().textRenderer.draw(matrices, text, 0, 0, color);
        matrices.pop();
    }

    static void renderItemIcon(ItemStack stack, double x, double y, float scale) {
        MatrixStack modelStack = RenderSystem.getModelViewStack();
        modelStack.push();
        modelStack.translate(x, y, 0);
        if (scale != 1) {
            modelStack.scale(scale, scale, 1);
        }
        RenderSystem.applyModelViewMatrix();

        MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(modelStack, stack, 0, 0);

        modelStack.pop();
        RenderSystem.applyModelViewMatrix();
    }

    static void renderRaceIcon(MatrixStack matrices, Race race, int x, int y, int size) {
        RenderSystem.setShaderTexture(0, race.getIcon());
        DrawableHelper.drawTexture(matrices, x - size / 2, y - size / 2, 0, 0, 0, size, size, size, size);
    }

    static void drawLine(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float r = (color >> 24 & 255) / 255F;
        float g = (color >> 16 & 255) / 255F;
        float b = (color >> 8 & 255) / 255F;
        float k = (color & 255) / 255F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y1, 0).color(r, g, b, k).next();
        bufferBuilder.vertex(matrix, x2, y2, 0).color(r, g, b, k).next();
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        RenderSystem.disableBlend();
    }

    /**
     * Renders a colored arc.
     *
     * @param mirrorHorizontally Whether or not the arc must be mirrored across the horizontal plane. Will produce a bar that grows from the middle filling both sides.
     */
    static void drawArc(MatrixStack matrices, double innerRadius, double outerRadius, double startAngle, double arcAngle, int color, boolean mirrorHorizontally) {
        float r = (color >> 24 & 255) / 255F;
        float g = (color >> 16 & 255) / 255F;
        float b = (color >> 8 & 255) / 255F;
        float k = (color & 255) / 255F;

        if (arcAngle < INCREMENT) {
            return;
        }

        final double maxAngle = MathHelper.clamp(startAngle + arcAngle, 0, TAU - INCREMENT);

        if (!mirrorHorizontally) {
            startAngle = -startAngle;
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f model = matrices.peek().getPositionMatrix();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (double angle = startAngle; angle >= -maxAngle; angle -= INCREMENT) {
            // center
            cylendricalVertex(bufferBuilder, model, innerRadius, angle, r, g, b, k);
            // point one
            cylendricalVertex(bufferBuilder, model, outerRadius, angle, r, g, b, k);
            // point two
            cylendricalVertex(bufferBuilder, model, outerRadius, angle + INCREMENT, r, g, b, k);
            // back to center
            cylendricalVertex(bufferBuilder, model, innerRadius, angle + INCREMENT, r, g, b, k);
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    /**
     * Renders hollow circle
     *
     * @param mirrorHorizontally Whether or not the arc must be mirrored across the horizontal plane. Will produce a bar that grows from the middle filling both sides.
     */
    static void drawArc(MatrixStack matrices, double radius, double startAngle, double arcAngle, int color, boolean mirrorHorizontally) {
        drawCircle(matrices, radius, startAngle, arcAngle, color, mirrorHorizontally, VertexFormat.DrawMode.DEBUG_LINES);
    }

    /**
     * Renders a filled circle.
     *
     * @param mirrorHorizontally Whether or not the arc must be mirrored across the horizontal plane. Will produce a bar that grows from the middle filling both sides.
     */
    static void drawCircle(MatrixStack matrices, double radius, double startAngle, double arcAngle, int color, boolean mirrorHorizontally) {
        drawCircle(matrices, radius, startAngle, arcAngle, color, mirrorHorizontally, VertexFormat.DrawMode.QUADS);
    }

    private static void drawCircle(MatrixStack matrices, double radius, double startAngle, double arcAngle, int color, boolean mirrorHorizontally, VertexFormat.DrawMode mode) {
        float r = (color >> 24 & 255) / 255F;
        float g = (color >> 16 & 255) / 255F;
        float b = (color >> 8 & 255) / 255F;
        float k = (color & 255) / 255F;

        if (arcAngle < INCREMENT) {
            return;
        }

        final double maxAngle = MathHelper.clamp(startAngle + arcAngle, 0, TAU - INCREMENT);

        if (!mirrorHorizontally) {
            startAngle = -startAngle;
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f model = matrices.peek().getPositionMatrix();

        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(mode, VertexFormats.POSITION_COLOR);

        boolean joinEnds = mode == VertexFormat.DrawMode.QUADS;

        // center

        for (double angle = startAngle; angle >= -maxAngle; angle -= INCREMENT) {
            if (joinEnds) {
                bufferBuilder.vertex(model, 0, 0, 0).color(r, g, b, k).next();
            }
            // point one
            cylendricalVertex(bufferBuilder, model, radius, angle, r, g, b, k);
            // point two
            cylendricalVertex(bufferBuilder, model, radius, angle + INCREMENT, r, g, b, k);
            if (joinEnds) {
                bufferBuilder.vertex(model, 0, 0, 0).color(r, g, b, k).next();
            }
        }

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    private static void cylendricalVertex(BufferBuilder bufferBuilder, Matrix4f model, double radius, double angle, float r, float g, float b, float k) {
        bufferBuilder.vertex(model,
                (float)(radius * MathHelper.sin((float)angle)),
                (float)(radius * MathHelper.cos((float)angle)), 0).color(r, g, b, k).normal(2, 2, 2).next();
    }
}
