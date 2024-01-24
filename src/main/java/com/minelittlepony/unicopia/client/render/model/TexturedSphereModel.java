package com.minelittlepony.unicopia.client.render.model;

import org.joml.Vector4f;

import com.minelittlepony.unicopia.client.gui.DrawableUtil;
import com.minelittlepony.unicopia.client.render.RenderUtil;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class TexturedSphereModel extends BakedModel {
    public static final TexturedSphereModel DISK = new TexturedSphereModel(40, 2, DrawableUtil.PI);

    public TexturedSphereModel(double rings, double sectors, double azimuthRange) {
        double zenithIncrement = DrawableUtil.PI / rings;
        double azimuthIncrement = DrawableUtil.TAU / sectors;
        SphereModel.compileVertices(azimuthRange, zenithIncrement, azimuthIncrement, this::addVertex);
    }

    @Override
    protected void addVertex(Vector4f vertex) {
        addVertex(vertex.x, vertex.y, vertex.z, vertex.x, vertex.z);
    }

    public final void render(MatrixStack matrices, VertexConsumer buffer, float scale, float r, float g, float b, float a, float uScale, float vScale) {
        scale = Math.abs(scale);
        if (scale < 0.001F) {
            return;
        }

        matrices.push();
        matrices.scale(scale, scale, scale);
        uScale *= 0.5F;
        vScale *= 0.5F;
        for (RenderUtil.Vertex vertex : vertices) {
            Vector4f pos = vertex.position(matrices);
            buffer.vertex(pos.x, pos.y, pos.z).texture((vertex.u() + 1) * uScale, (vertex.v() + 1) * vScale).color(r, g, b, a).next();
        }
        matrices.pop();
    }
}
