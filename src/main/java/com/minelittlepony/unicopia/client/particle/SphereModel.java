package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.math.Vector4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

public class SphereModel {

    protected Vec3d pos;

    protected Quaternion rotX = Quaternion.IDENTITY;
    protected Quaternion rotY = Quaternion.IDENTITY;
    protected Quaternion rotZ = Quaternion.IDENTITY;

    public void setPosition(double x, double y, double z) {
        pos = new Vec3d(x, y, z);
    }

    public void setRotation(float x, float y, float z) {
        rotX = Vec3f.POSITIVE_X.getDegreesQuaternion(x);
        rotY = Vec3f.POSITIVE_Y.getDegreesQuaternion(y);
        rotZ = Vec3f.POSITIVE_Z.getDegreesQuaternion(z);
    }

    public void render(MatrixStack matrices, float scale, VertexConsumer vertexWriter, int light, int overlay, float r, float g, float b, float a) {
        if (scale == 0) {
            return;
        }

        matrices.push();

        matrices.translate(pos.x, pos.y, pos.z);
        matrices.multiply(rotX);
        matrices.multiply(rotY);
        matrices.multiply(rotZ);

        matrices.scale(scale, scale, scale);

        render(matrices.peek(), vertexWriter, light, overlay, r, g, b, a);

        matrices.pop();
    }

    public void render(MatrixStack.Entry matrices, VertexConsumer vertexWriter, int light, int overlay, float r, float g, float b, float a) {

        Matrix4f model = matrices.getModel();

        final double num_rings = 40;
        final double num_sectors = 40;
        final double pi = Math.PI;
        final double two_pi = Math.PI * 2F;
        final double zenithIncrement = Math.PI / num_rings;
        final double azimuthIncrement = two_pi / num_sectors;

        double radius = 1;

        for(double zenith = -pi; zenith < pi; zenith += zenithIncrement) {
            for(double azimuth = -two_pi; azimuth < two_pi; azimuth += azimuthIncrement) {
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
