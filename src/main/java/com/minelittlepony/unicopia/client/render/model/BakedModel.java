package com.minelittlepony.unicopia.client.render.model;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;
import org.joml.Vector4f;

import com.minelittlepony.unicopia.client.render.RenderUtil;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class BakedModel {
    protected final List<RenderUtil.Vertex> vertices = new ArrayList<>();

    protected void addVertex(Vector4f vertex) {
        addVertex(vertex.x, vertex.y, vertex.z, (vertex.x + 1) * 0.5F, (vertex.z + 1) * 0.5F);
    }

    protected void addVertex(float x, float y, float z, float u, float v) {
        vertices.add(new RenderUtil.Vertex(new Vector3f(x, y, z), u, v));
    }

    public final void render(MatrixStack matrices, VertexConsumer buffer, int light, int overlay, float scale, float r, float g, float b, float a) {
        scale = Math.abs(scale);
        if (scale < 0.001F) {
            return;
        }

        matrices.push();
        matrices.scale(scale, scale, scale);
        for (RenderUtil.Vertex vertex : vertices) {
            Vector4f pos = vertex.position(matrices);
            buffer.vertex(pos.x, pos.y, pos.z, r, g, b, a, vertex.u(), vertex.v(), overlay, light, 0, 0, 0);
        }
        matrices.pop();
    }

    public final void render(MatrixStack matrices, VertexConsumer buffer, float scale, float r, float g, float b, float a, float uScale, float vScale) {
        scale = Math.abs(scale);
        if (scale < 0.001F) {
            return;
        }

        matrices.push();
        matrices.scale(scale, scale, scale);
        for (RenderUtil.Vertex vertex : vertices) {
            Vector4f pos = vertex.position(matrices);
            buffer.vertex(pos.x, pos.y, pos.z).texture(vertex.u() * uScale, vertex.v() * vScale).color(r, g, b, a).next();
        }
        matrices.pop();
    }
}
