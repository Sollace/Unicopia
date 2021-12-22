package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vector4f;

public class SphereModel {
    public void render(MatrixStack matrices, VertexConsumer vertexWriter, int light, int overlay, float r, float g, float b, float a) {
        render(matrices.peek(), vertexWriter, light, overlay, r, g, b, a);
    }

    public void render(MatrixStack.Entry matrices, VertexConsumer vertexWriter, int light, int overlay, float r, float g, float b, float a) {

        Matrix4f model = matrices.getPositionMatrix();

        final double num_rings = 40;
        final double num_sectors = 40;
        final double pi = Math.PI;
        final double two_pi = Math.PI * 2F;
        final double zenithIncrement = Math.PI / num_rings;
        final double azimuthIncrement = two_pi / num_sectors;

        double radius = 1;

        for (double zenith = -pi; zenith < pi; zenith += zenithIncrement) {
            for (double azimuth = -two_pi; azimuth < two_pi; azimuth += azimuthIncrement) {
                drawQuad(model, vertexWriter, radius, zenith, azimuth, zenithIncrement, azimuthIncrement, light, overlay, r, g, b, a);
            }
        }
    }

    protected void drawQuad(Matrix4f model, VertexConsumer vertexWriter,
            double radius, double zenith, double azimuth,
            double zenithIncrement, double azimuthIncrement,
            int light, int overlay, float r, float g, float b, float a) {

        drawVertex(model, vertexWriter, radius, zenith, azimuth, light, overlay, r, g, b, a);

        drawVertex(model, vertexWriter, radius, zenith + zenithIncrement, azimuth, light, overlay, r, g, b, a);

        drawVertex(model, vertexWriter, radius, zenith + zenithIncrement, azimuth + azimuthIncrement, light, overlay, r, g, b, a);

        drawVertex(model, vertexWriter, radius, zenith, azimuth + azimuthIncrement, light, overlay, r, g, b, a);
    }

    public static void drawVertex(Matrix4f model, VertexConsumer vertexWriter,
            double radius, double zenith, double azimuth,
            int light, int overlay, float r, float g, float b, float a) {
        Vector4f position = convertToCartesianCoord(radius, zenith, azimuth);
        position.transform(model);
        vertexWriter.vertex(position.getX(), position.getY(), position.getZ(), r, g, b, a, 0, 0, overlay, light, 0, 0, 0);
    }

    public static Vector4f convertToCartesianCoord(double r, double theta, double phi) {

        double x = r * Math.sin(theta) * Math.cos(phi);
        double y = r * Math.sin(theta) * Math.sin(phi);
        double z = r * Math.cos(theta);

        return new Vector4f((float)x, (float)y, (float)z, 1);
    }
}
