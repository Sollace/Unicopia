package com.minelittlepony.unicopia.client.render.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;

public class BakedModel {
    private static final Vector4f drawVert = new Vector4f();

    protected final List<Vertex> vertices = new ArrayList<>();

    protected void addVertex(Vector4f vertex) {
        addVertex(vertex.getX(), vertex.getY(), vertex.getZ(), 0, 0);
    }

    protected void addVertex(float x, float y, float z, float u, float v) {
        vertices.add(new Vertex(x, y, z, u, v));
    }

    public final void render(MatrixStack matrices, VertexConsumer vertexWriter, int light, int overlay, float scale, float r, float g, float b, float a) {
        scale = Math.abs(scale);
        if (scale < 0.001F) {
            return;
        }

        Matrix4f model = matrices.peek().getPositionMatrix();
        for (Vertex vertex : vertices) {
            drawVert.set(vertex.x() * scale, vertex.y() * scale, vertex.z() * scale, 1);
            drawVert.transform(model);
            vertexWriter.vertex(drawVert.getX(), drawVert.getY(), drawVert.getZ(), r, g, b, a, vertex.u(), vertex.v(), overlay, light, 0, 0, 0);
        }
    }

    record Vertex(float x, float y, float z, float u, float v) {}
}
