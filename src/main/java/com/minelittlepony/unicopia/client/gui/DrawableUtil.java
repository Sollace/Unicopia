package com.minelittlepony.unicopia.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

public class DrawableUtil {
    public static final double PI = Math.PI;
    public static final double TAU = Math.PI * 2;
    private static final double NUM_RINGS = 300;
    private static final double INCREMENT = TAU / NUM_RINGS;


    public static void renderItemIcon(ItemStack stack, double x, double y, float scale) {
        MatrixStack modelStack = RenderSystem.getModelViewStack();
        modelStack.push();
        modelStack.translate(x, y, 0);
        if (scale != 1) {
            modelStack.scale(scale, scale, 1);
        }
        RenderSystem.applyModelViewMatrix();

        MinecraftClient.getInstance().getItemRenderer().renderGuiItemIcon(stack, 0, 0);

        modelStack.pop();
        RenderSystem.applyModelViewMatrix();
    }

    public static void drawLine(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float r = (color >> 24 & 255) / 255F;
        float g = (color >> 16 & 255) / 255F;
        float b = (color >> 8 & 255) / 255F;
        float k = (color & 255) / 255F;
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x1, y1, 0).color(r, g, b, k).next();
        bufferBuilder.vertex(matrix, x2, y2, 0).color(r, g, b, k).next();
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    /**
     * Renders a colored arc.
     *
     * @param mirrorHorizontally Whether or not the arc must be mirrored across the horizontal plane. Will produce a bar that grows from the middle filling both sides.
     */
    public static void drawArc(MatrixStack matrices, double innerRadius, double outerRadius, double startAngle, double arcAngle, int color, boolean mirrorHorizontally) {
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
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
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

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
    }

    /**
     * Renders hollow circle
     *
     * @param mirrorHorizontally Whether or not the arc must be mirrored across the horizontal plane. Will produce a bar that grows from the middle filling both sides.
     */
    public static void drawArc(MatrixStack matrices, double radius, double startAngle, double arcAngle, int color, boolean mirrorHorizontally) {
        drawCircle(matrices, radius, startAngle, arcAngle, color, mirrorHorizontally, VertexFormat.DrawMode.DEBUG_LINES);
    }

    /**
     * Renders a filled circle.
     *
     * @param mirrorHorizontally Whether or not the arc must be mirrored across the horizontal plane. Will produce a bar that grows from the middle filling both sides.
     */
    public static void drawCircle(MatrixStack matrices, double radius, double startAngle, double arcAngle, int color, boolean mirrorHorizontally) {
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
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
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

        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.enableTexture();
    }

    private static void cylendricalVertex(BufferBuilder bufferBuilder, Matrix4f model, double radius, double angle, float r, float g, float b, float k) {
        bufferBuilder.vertex(model,
                (float)(radius * MathHelper.sin((float)angle)),
                (float)(radius * MathHelper.cos((float)angle)), 0).color(r, g, b, k).normal(2, 2, 2).next();
    }
}
