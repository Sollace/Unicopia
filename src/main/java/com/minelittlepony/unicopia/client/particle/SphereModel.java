package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vector4f;

public class SphereModel {
    protected static final double pi = Math.PI;
    protected static final double two_pi = pi * 2F;

    public static final SphereModel SPHERE = new SphereModel(40, 40, two_pi);
    public static final SphereModel DISK = new SphereModel(40, 2, pi);

    private final double num_rings;
    private final double num_sectors;

    private final double azimuthRange;

    final double zenithIncrement;
    final double azimuthIncrement;

    public SphereModel(double rings, double sectors, double azimuthRange) {
        this.num_rings = rings;
        this.num_sectors = sectors;
        this.azimuthRange = azimuthRange;

        zenithIncrement = pi / num_rings;
        azimuthIncrement = two_pi / num_sectors;
    }

    public final void render(MatrixStack matrices, VertexConsumer vertexWriter, int light, int overlay, float radius, float r, float g, float b, float a) {
        if (radius <= 0) {
            return;
        }

        Matrix4f position = matrices.peek().getPositionMatrix();

        for (double zenith = -pi; zenith < pi; zenith += zenithIncrement) {
            for (double azimuth = -azimuthRange; azimuth < azimuthRange; azimuth += azimuthIncrement) {
                drawQuad(position, vertexWriter, radius, zenith, azimuth, light, overlay, r, g, b, a);
            }
        }
    }

    private void drawQuad(Matrix4f model, VertexConsumer vertexWriter,
            double radius, double zenith, double azimuth,
            int light, int overlay, float r, float g, float b, float a) {
        drawVertex(model, vertexWriter, convertToCartesianCoord(radius, zenith, azimuth), light, overlay, r, g, b, a);
        drawVertex(model, vertexWriter, convertToCartesianCoord(radius, zenith + zenithIncrement, azimuth), light, overlay, r, g, b, a);
        drawVertex(model, vertexWriter, convertToCartesianCoord(radius, zenith + zenithIncrement, azimuth + azimuthIncrement), light, overlay, r, g, b, a);
        drawVertex(model, vertexWriter, convertToCartesianCoord(radius, zenith, azimuth + azimuthIncrement), light, overlay, r, g, b, a);
    }

    private static void drawVertex(Matrix4f model, VertexConsumer vertexWriter, Vector4f position, int light, int overlay, float r, float g, float b, float a) {
        position.transform(model);
        vertexWriter.vertex(position.getX(), position.getY(), position.getZ(), r, g, b, a, 0, 0, overlay, light, 0, 0, 0);
    }

    private static Vector4f convertToCartesianCoord(double r, double theta, double phi) {
        double x = r * Math.sin(theta) * Math.cos(phi);
        double y = r * Math.sin(theta) * Math.sin(phi);
        double z = r * Math.cos(theta);

        return new Vector4f((float)x, (float)y, (float)z, 1);
    }
}
