package com.minelittlepony.unicopia.client.render.model;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class BakedModel {
    private static final Vector4f drawVert = new Vector4f();

    protected final List<Vertex> vertices = new ArrayList<>();

    protected void addVertex(Vector4f vertex) {
        addVertex(vertex.x, vertex.y, vertex.z, 0, 0);
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
            drawVert.mul(model);
            vertexWriter.vertex(drawVert.x, drawVert.y, drawVert.z, r, g, b, a, vertex.u(), vertex.v(), overlay, light, 0, 0, 0);
        }
    }

    record Vertex(float x, float y, float z, float u, float v) {}
}
