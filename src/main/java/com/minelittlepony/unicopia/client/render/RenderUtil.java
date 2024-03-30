package com.minelittlepony.unicopia.client.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction;

public class RenderUtil {
    public static final Vector4f TEMP_VECTOR = new Vector4f();
    private static final Vector4f TEMP_UV_VECTOR = new Vector4f();
    public static final Vertex[] UNIT_FACE = new Vertex[] {
            new Vertex(0, 0, 0, 1, 1),
            new Vertex(0, 1, 0, 1, 0),
            new Vertex(1, 1, 0, 0, 0),
            new Vertex(1, 0, 0, 0, 1)
    };
    public static final Vertex[] FRAME_BUFFER_VERTICES = new Vertex[] {
            new Vertex(0, 1, 0, 0, 0),
            new Vertex(1, 1, 0, 1, 0),
            new Vertex(1, 0, 0, 1, 1),
            new Vertex(0, 0, 0, 0, 1)
    };
    private static final Vertex[][] CUBE_VERTICES = {
            new Vertex[] { // down
                    new Vertex(0, 0, 0, 0, 0),
                    new Vertex(1, 0, 0, 1, 0),
                    new Vertex(1, 0, 1, 1, 1),
                    new Vertex(0, 0, 1, 0, 1)
            },
            new Vertex[] { //up
                    new Vertex(0, 1, 0, 0, 0),
                    new Vertex(0, 1, 1, 0, 1),
                    new Vertex(1, 1, 1, 1, 1),
                    new Vertex(1, 1, 0, 1, 0)
            },
            new Vertex[] { //north
                    new Vertex(0, 0, 0, 0, 0),
                    new Vertex(0, 1, 0, 0, 1),
                    new Vertex(1, 1, 0, 1, 1),
                    new Vertex(1, 0, 0, 1, 0)
            },
            new Vertex[] { //south
                    new Vertex(0, 0, 1, 0, 0),
                    new Vertex(1, 0, 1, 1, 0),
                    new Vertex(1, 1, 1, 1, 1),
                    new Vertex(0, 1, 1, 0, 1)
            },
            new Vertex[] { //west
                    new Vertex(0, 0, 0, 0, 0),
                    new Vertex(0, 0, 1, 1, 0),
                    new Vertex(0, 1, 1, 1, 1),
                    new Vertex(0, 1, 0, 0, 1)
            },
            new Vertex[] { //east
                    new Vertex(1, 0, 0, 0, 0),
                    new Vertex(1, 1, 0, 1, 0),
                    new Vertex(1, 1, 1, 1, 1),
                    new Vertex(1, 0, 1, 0, 1)
            }
    };

    public static void renderSpriteCubeFaces(MatrixStack matrices, VertexConsumerProvider provider, Sprite sprite,
            float width, float height, float length,
            int color, int light, int overlay,
            Direction... directions) {
        float r = ColorHelper.Abgr.getRed(color),
            g = ColorHelper.Abgr.getGreen(color),
            b = ColorHelper.Abgr.getBlue(color),
            a = ColorHelper.Abgr.getAlpha(color);
        float u0 = sprite.getMinU(), uDelta = sprite.getMaxU() - u0;
        float v0 = sprite.getMinV(), vDelta = sprite.getMaxV() - v0;
        RenderLayer layer = RenderLayer.getEntitySolid(sprite.getAtlasId());
        VertexConsumer buffer = provider.getBuffer(layer);
        Matrix4f position = matrices.peek().getPositionMatrix();
        for (Direction direction : directions) {
            for (Vertex vertex : CUBE_VERTICES[direction.ordinal()]) {
                Vector4f pos = position.transform(TEMP_VECTOR.set(vertex.position(), 1).mul(width, height, length, 1));
                buffer.vertex(
                        pos.x, pos.y, pos.z,
                        r, g, b, a,
                        u0 + vertex.texture().x * uDelta,
                        v0 + vertex.texture().y * vDelta,
                        overlay, light,
                        direction.getOffsetX(), direction.getOffsetY(), direction.getOffsetZ()
                );
            }
        }
    }

    public static void renderFace(MatrixStack matrices, Tessellator te, BufferBuilder buffer, float r, float g, float b, float a, int light) {
        renderFace(matrices, te, buffer, r, g, b, a, light, 1, 1);
    }

    public static void renderFace(MatrixStack matrices, Tessellator te, BufferBuilder buffer, float r, float g, float b, float a, int light, float uScale, float vScale) {
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        Matrix4f positionmatrix = matrices.peek().getPositionMatrix();
        for (Vertex vertex : UNIT_FACE) {
            Vector4f position = vertex.position(positionmatrix);
            buffer.vertex(position.x, position.y, position.z).texture(vertex.texture().x * uScale, vertex.texture().y * vScale).color(r, g, b, a).light(light).next();
        }
        te.draw();
    }

    public record Vertex(Vector3f position, Vector3f texture) {
        public Vertex(float x, float y, float z, float u, float v) {
            this(new Vector3f(x, y, z), new Vector3f(u, v, 1));
        }

        public Vector4f position(Matrix4f mat) {
            return mat.transform(TEMP_VECTOR.set(position, 1));
        }

        public Vector4f texture(Matrix4f mat) {
            return mat.transform(TEMP_UV_VECTOR.set(texture, 1));
        }
    }
}
