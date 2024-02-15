package com.minelittlepony.unicopia.client.render.model;

import java.util.List;

import org.joml.Matrix4f;
import org.joml.Vector4f;

import com.minelittlepony.unicopia.client.render.RenderUtil;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class BakedModel {
    protected final List<RenderUtil.Vertex> vertices = new ObjectArrayList<>();

    private final Matrix4f textureMatrix = new Matrix4f();

    public Matrix4f getTextureMatrix() {
        return textureMatrix;
    }

    public void scaleUV(float uScale, float vScale) {
        getTextureMatrix().scale(uScale, vScale, 1);
    }

    protected void addVertex(Vector4f vertex) {
        addVertex(vertex.x, vertex.y, vertex.z, (vertex.x + 1) * 0.5F, (vertex.z + 1) * 0.5F);
    }

    protected void addVertex(float x, float y, float z, float u, float v) {
        vertices.add(new RenderUtil.Vertex(x, y, z, u, v));
    }

    public final void render(MatrixStack matrices, VertexConsumer buffer, int light, int overlay, float scale, float r, float g, float b, float a) {
        scale = Math.abs(scale);
        if (scale < 0.001F) {
            return;
        }

        matrices.push();
        matrices.scale(scale, scale, scale);
        Matrix4f positionmatrix = matrices.peek().getPositionMatrix();
        for (RenderUtil.Vertex vertex : vertices) {
            Vector4f pos = vertex.position(positionmatrix);
            Vector4f tex = vertex.texture(textureMatrix);
            buffer.vertex(pos.x, pos.y, pos.z, r, g, b, a, tex.x, tex.y, overlay, light, 0, 0, 0);
        }
        matrices.pop();
        textureMatrix.identity();
    }

    public final void render(MatrixStack matrices, VertexConsumer buffer, int light, float scale, float r, float g, float b, float a) {
        scale = Math.abs(scale);
        if (scale < 0.001F) {
            return;
        }

        matrices.push();
        matrices.scale(scale, scale, scale);
        Matrix4f positionmatrix = matrices.peek().getPositionMatrix();
        for (RenderUtil.Vertex vertex : vertices) {
            Vector4f pos = vertex.position(positionmatrix);
            Vector4f tex = vertex.texture(textureMatrix);
            buffer.vertex(pos.x, pos.y, pos.z).texture(tex.x, tex.y).color(r, g, b, a).light(getLightAt(pos, light)).next();
        }
        matrices.pop();
        textureMatrix.identity();
    }

    protected int getLightAt(Vector4f pos, int light) {
        return light;
    }
}
