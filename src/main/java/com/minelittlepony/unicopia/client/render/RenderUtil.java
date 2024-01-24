package com.minelittlepony.unicopia.client.render;

import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

public class RenderUtil {
    public static final Vector4f TEMP_VECTOR = new Vector4f();
    public static final Vertex[] UNIT_FACE = new Vertex[] {
            new Vertex(new Vector3f(0, 0, 0), 1, 1),
            new Vertex(new Vector3f(0, 1, 0), 1, 0),
            new Vertex(new Vector3f(1, 1, 0), 0, 0),
            new Vertex(new Vector3f(1, 0, 0), 0, 1)
    };
    public static final Vertex[] FRAME_BUFFER_VERTICES = new Vertex[] {
            new Vertex(new Vector3f(0, 1, 0), 0, 0),
            new Vertex(new Vector3f(1, 1, 0), 1, 0),
            new Vertex(new Vector3f(1, 0, 0), 1, 1),
            new Vertex(new Vector3f(0, 0, 0), 0, 1)
    };

    public static void renderFace(MatrixStack matrices, Tessellator te, BufferBuilder buffer, float r, float g, float b, float a, int light) {
        renderFace(matrices, te, buffer, r, g, b, a, light, 1, 1);
    }

    public static void renderFace(MatrixStack matrices, Tessellator te, BufferBuilder buffer, float r, float g, float b, float a, int light, float uScale, float vScale) {
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        for (Vertex vertex : UNIT_FACE) {
            Vector4f position = vertex.position(matrices);
            buffer.vertex(position.x, position.y, position.z).texture(vertex.u() * uScale, vertex.v() * vScale).color(r, g, b, a).light(light).next();
        }
        te.draw();
    }

    public record Vertex(Vector3f position, float u, float v) {
        public Vector4f position(MatrixStack matrices) {
            matrices.peek().getPositionMatrix().transform(TEMP_VECTOR.set(position, 1));
            return TEMP_VECTOR;
        }
    }
}
