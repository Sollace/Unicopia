package com.minelittlepony.unicopia.client.render.model;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;

public class DiskModel extends SphereModel {

    @Override
    public void render(MatrixStack.Entry matrices, VertexConsumer vertexWriter, int light, int overlay, float r, float g, float b, float a) {
        Matrix4f model = matrices.getModel();

        final double num_rings = 30;
        final double zenithIncrement = Math.PI / num_rings;

        double radius = 1;

        for(double zenith = 0; zenith < Math.PI; zenith += zenithIncrement) {
            drawVertex(model, vertexWriter, radius, zenith, Math.PI, light, overlay, r, g, b, a);
        }
    }

}
