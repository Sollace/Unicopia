package com.minelittlepony.unicopia.client.render.model;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.client.util.math.Vector4f;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;

public class SphereModel {

    protected Vec3d pos;

    protected Quaternion rotX;
    protected Quaternion rotY;
    protected Quaternion rotZ;

    public void setPosition(double x, double y, double z) {
        pos = new Vec3d(x, y, z).subtract(BlockEntityRenderDispatcher.INSTANCE.camera.getPos());
    }

    public void setRotation(float x, float y, float z) {
        rotX = Vector3f.POSITIVE_X.getDegreesQuaternion(x);
        rotY = Vector3f.POSITIVE_Y.getDegreesQuaternion(y);
        rotZ = Vector3f.POSITIVE_Z.getDegreesQuaternion(z);
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

        final double num_rings = 30;
        final double num_sectors = 30;
        final double two_pi = Math.PI * 2;
        final double zenithIncrement = Math.PI / num_rings;
        final double azimuthIncrement = two_pi / num_sectors;

        double radius = 1;

        for(double zenith = 0; zenith < Math.PI; zenith += zenithIncrement) {
            for(double azimuth = 0; azimuth < two_pi; azimuth += azimuthIncrement) {
                drawVertex(model, vertexWriter, radius, zenith, azimuth, light, overlay, r, g, b, a);                                      // top left
                drawVertex(model, vertexWriter, radius, zenith + zenithIncrement, azimuth, light, overlay, r, g, b, a);                    // top right
                drawVertex(model, vertexWriter, radius, zenith + zenithIncrement, azimuth + azimuthIncrement, light, overlay, r, g, b, a); // bottom right
                drawVertex(model, vertexWriter, radius, zenith, azimuth + azimuthIncrement, light, overlay, r, g, b, a);                   // bottom left
            }
        }
    }

    protected void drawQuad(Matrix4f model, VertexConsumer vertexWriter,
            double radius, double zenith, double azimuth,
            double zenithIncrement, double azimuthIncrement,
            int light, int overlay, float r, float g, float b, float a) {


    }

    protected void drawVertex(Matrix4f model, VertexConsumer vertexWriter,
            double radius, double zenith, double azimuth,
            int light, int overlay, float r, float g, float b, float a) {
        Vector4f position = convertToCartesianCoord(radius, zenith, azimuth);
        position.transform(model);
        vertexWriter.vertex(position.getX(), position.getY(), position.getZ(),
                r, g, b, a,
                0, 0, overlay, light, 0, 0, 0);
    }

    protected Vector4f convertToCartesianCoord(double radius, double zenith, double azimuth) {

        double tanq = Math.tan(zenith);

        double x = Math.pow(radius, 2) / (2 * tanq) - (tanq / 2);
        double y = x * tanq;
        double z = radius / Math.tan(azimuth);

        return new Vector4f((float)x, (float)y, (float)z, 1);
    }
}
