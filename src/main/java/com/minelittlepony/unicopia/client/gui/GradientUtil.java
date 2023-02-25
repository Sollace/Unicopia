package com.minelittlepony.unicopia.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;

public interface GradientUtil {

    static void fillVerticalGradient(MatrixStack matrices, int startX, int startY, int stopY, int endX, int endY, int colorStart, int colorStop, int colorEnd, int z) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        fillVerticalGradient(matrices.peek().getPositionMatrix(), bufferBuilder, startX, startY, stopY, endX, endY, z, colorStart, colorStop, colorEnd);
        tessellator.draw();
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    private static void fillVerticalGradient(Matrix4f matrix, BufferBuilder builder, int startX, int startY, int stopY, int endX, int endY, int z, int colorStart, int colorStop, int colorEnd) {
        final float fromA = (colorStart >> 24 & 0xFF) / 255F;
        final float fromR = (colorStart >> 16 & 0xFF) / 255F;
        final float fromG = (colorStart >> 8 & 0xFF) / 255F;
        final float fromB = (colorStart & 0xFF) / 255F;

        final float stopA = (colorStop >> 24 & 0xFF) / 255F;
        final float stopR = (colorStop >> 16 & 0xFF) / 255F;
        final float stopG = (colorStop >> 8 & 0xFF) / 255F;
        final float stopB = (colorStop & 0xFF) / 255F;

        final float toA = (colorEnd >> 24 & 0xFF) / 255F;
        final float toR = (colorEnd >> 16 & 0xFF) / 255F;
        final float toG = (colorEnd >> 8 & 0xFF) / 255F;
        final float toB = (colorEnd & 0xFF) / 255F;

        builder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        builder.vertex(matrix, endX, stopY, z).color(stopR, stopG, stopB, stopA).next();
        builder.vertex(matrix, endX, startY, z).color(fromR, fromG, fromB, fromA).next();
        builder.vertex(matrix, startX, startY, z).color(fromR, fromG, fromB, fromA).next();
        builder.vertex(matrix, startX, stopY, z).color(stopR, stopG, stopB, stopA).next();
        builder.vertex(matrix, startX, endY, z).color(toR, toG, toB, toA).next();
        builder.vertex(matrix, endX, endY, z).color(stopR, toG, toB, toA).next();
    }

    static void fillRadialGradient(MatrixStack matrices, int startX, int startY, int endX, int endY, int colorStart, int colorEnd, int z, float radius) {
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tessellator tessellator = Tessellator.getInstance();
        if (fillRadials(matrices.peek().getPositionMatrix(), tessellator.getBuffer(), startX, startY, endX, endY, z, colorStart, colorEnd, radius)) {
            tessellator.draw();
        }
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    private static boolean fillRadials(Matrix4f matrix, BufferBuilder builder, int startX, int startY, int endX, int endY, int z, int colorStart, int colorEnd, float radius) {
        final float fromA = (colorStart >> 24 & 0xFF) / 255F;
        final float fromR = (colorStart >> 16 & 0xFF) / 255F;
        final float fromG = (colorStart >> 8 & 0xFF) / 255F;
        final float fromB = (colorStart & 0xFF) / 255F;
        final float toA = (colorEnd >> 24 & 0xFF) / 255F;
        final float toR = (colorEnd >> 16 & 0xFF) / 255F;
        final float toG = (colorEnd >> 8 & 0xFF) / 255F;
        final float toB = (colorEnd & 0xFF) / 255F;

        double increment = DrawableUtil.TAU / 30D;

        float width = endX - startX;
        float height = endY - startY;

        float outerRadius = MathHelper.sqrt((width * width) + (height * height)) / 2F;

        float innerRadius = outerRadius * (1 - radius);

        builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        for (double angle = 0; angle < DrawableUtil.TAU; angle += increment) {
            clampedVertex(builder, matrix, innerRadius, angle + increment, z, startX, endX, startY, endY).color(toR, toG, toB, toA).next();
            clampedVertex(builder, matrix, innerRadius, angle, z, startX, endX, startY, endY).color(toR, toG, toB, toA).next();
            clampedVertex(builder, matrix, outerRadius, angle, z, startX, endX, startY, endY).color(fromR, fromG, fromB, fromA).next();
            clampedVertex(builder, matrix, outerRadius, angle + increment, z, startX, endX, startY, endY).color(fromR, fromG, fromB, fromA).next();
        }
        return true;
    }

    static float getX(double radius, double angle) {
        return (float)(radius * MathHelper.sin((float)angle));
    }

    static float getY(double radius, double angle) {
        return (float)(radius * MathHelper.cos((float)angle));
    }

    private static VertexConsumer clampedVertex(BufferBuilder bufferBuilder, Matrix4f model, double radius, double angle, float z, int minX, int maxX, int minY, int maxY) {
        float midX = (maxX - minX) / 2F;
        float midY = (maxY - minY) / 2F;

        float x = midX + getX(radius, angle);
        float y = midY + getY(radius, angle);

        float xPad = (maxX - minX) / 10F;
        float yPad = (maxY - minY) / 20F;

        if (x < minX + xPad) {
            x = minX;
        }
        if (x > maxX - xPad) {
            x = maxX;
        }

        if (y < minY + yPad) {
            y = minY;
        }
        if (y > maxY - yPad) {
            y = maxY;
        }

        return bufferBuilder.vertex(model, x, y, z);
    }
}
