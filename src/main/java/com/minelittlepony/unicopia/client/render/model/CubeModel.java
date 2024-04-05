package com.minelittlepony.unicopia.client.render.model;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.minelittlepony.common.util.Color;
import com.minelittlepony.unicopia.client.render.RenderUtil;
import com.minelittlepony.unicopia.client.render.RenderUtil.Vertex;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;

public class CubeModel {
    private static final Vector2f TEMP_UV_VECTOR = new Vector2f();
    private static final Vertex[][] CUBE_VERTICES = {
            new Vertex[] {
                    new Vertex(0, 0, 0, 0, 0),
                    new Vertex(1, 0, 0, 1, 0),
                    new Vertex(1, 0, 1, 1, 1),
                    new Vertex(0, 0, 1, 0, 1)
            },
            new Vertex[] {
                    new Vertex(0, 1, 0, 0, 0),
                    new Vertex(0, 1, 1, 0, 1),
                    new Vertex(1, 1, 1, 1, 1),
                    new Vertex(1, 1, 0, 1, 0)
            },
            new Vertex[] {
                    new Vertex(0, 0, 0, 0, 0),
                    new Vertex(0, 1, 0, 0, 1),
                    new Vertex(1, 1, 0, 1, 1),
                    new Vertex(1, 0, 0, 1, 0)
            },
            new Vertex[] {
                    new Vertex(0, 0, 1, 0, 0),
                    new Vertex(1, 0, 1, 1, 0),
                    new Vertex(1, 1, 1, 1, 1),
                    new Vertex(0, 1, 1, 0, 1)
            },
            new Vertex[] {
                    new Vertex(0, 0, 0, 0, 0),
                    new Vertex(0, 0, 1, 1, 0),
                    new Vertex(0, 1, 1, 1, 1),
                    new Vertex(0, 1, 0, 0, 1)
            },
            new Vertex[] {
                    new Vertex(1, 0, 0, 0, 0),
                    new Vertex(1, 1, 0, 1, 0),
                    new Vertex(1, 1, 1, 1, 1),
                    new Vertex(1, 0, 1, 0, 1)
            }
    };

    public static void render(MatrixStack matrices, VertexConsumer buffer,
            float u0, float v0, float u1, float v1,
            float x0, float y0, float z0, float x1, float y1, float z1,
            int color, int light, int overlay,
            Direction... directions) {
        float r = Color.r(color), g = Color.g(color), b = Color.b(color);
        float du = u1 - u0, dv = v1 - v0;
        float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        Matrix4f position = matrices.peek().getPositionMatrix();
        Matrix3f normal = matrices.peek().getNormalMatrix();
        for (Direction direction : directions) {
            for (Vertex vertex : CUBE_VERTICES[direction.ordinal()]) {
                Vector4f pos = position.transform(RenderUtil.TEMP_VECTOR.set(vertex.position(), 1).mul(dx, dy, dz, 1).add(x0, y0, z0, 0));
                Vector2f tex = TEMP_UV_VECTOR.set(vertex.texture().x, vertex.texture().y).mul(du, dv).add(u0, v0);
                Vector3f norm = normal.transform(RenderUtil.TEMP_NORMAL_VECTOR.set(direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ()));
                buffer.vertex(pos.x, pos.y, pos.z, r, g, b, 1, tex.x, tex.y, overlay, light, norm.x, norm.y, norm.z);
            }
        }
    }
}
