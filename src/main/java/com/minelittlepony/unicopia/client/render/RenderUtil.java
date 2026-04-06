package com.minelittlepony.unicopia.client.render;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.include.com.google.common.base.Preconditions;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;

public class RenderUtil {
    public static final Vector4f TEMP_VECTOR = new Vector4f();
    public static final Vector4f TEMP_UV_VECTOR = new Vector4f();
    public static final Vector3f TEMP_NORMAL_VECTOR = new Vector3f();
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

    public static void renderFace(MatrixStack matrices, VertexConsumer buffer, float r, float g, float b, float a, int light) {
        renderFace(matrices, buffer, r, g, b, a, light, 1, 1);
    }

    public static void renderFace(MatrixStack matrices, VertexConsumer buffer, float r, float g, float b, float a, int light, float uScale, float vScale) {
        Matrix4f positionmatrix = matrices.peek().getPositionMatrix();
        for (Vertex vertex : UNIT_FACE) {
            Vector4f position = vertex.position(positionmatrix);
            buffer.vertex(position.x, position.y, position.z).texture(vertex.texture().x * uScale, vertex.texture().y * vScale).color(r, g, b, a).light(light);
        }
    }

    public static void copyBufferToBuffer(Framebuffer from, Framebuffer to) {
        var commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        Preconditions.checkArgument(from.textureWidth == to.textureWidth && from.textureHeight == to.textureHeight, "Buffers must be the same size");

        commandEncoder.copyTextureToTexture(to.getDepthAttachment(), from.getDepthAttachment(), 0, 0, 0, 0, 0, from.textureWidth, from.textureHeight);
        commandEncoder.copyTextureToTexture(to.getColorAttachment(), from.getColorAttachment(), 0, 0, 0, 0, 0, from.textureWidth, from.textureHeight);
    }

    public static void copyBufferToTexture(Framebuffer from, GpuTexture to) {
        RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(to, from.getColorAttachment(), 0, 0, 0, 0, 0, from.textureWidth, from.textureHeight);
    }

    public record Vertex(Vector3f position, Vector3f texture) {
        public Vertex(float x, float y, float z, float u, float v) {
            this(new Vector3f(x, y, z), new Vector3f(u, v, 1));
        }

        public Vertex() {
            this(new Vector3f(), new Vector3f());
        }

        public void set(float x, float y, float z, float u, float v) {
            position.set(x, y, z);
            texture.set(u, v, 1);
        }

        public Vector4f position(Matrix4f mat) {
            return mat.transform(TEMP_VECTOR.set(position, 1));
        }

        public Vector4f texture(Matrix4f mat) {
            return mat.transform(TEMP_UV_VECTOR.set(texture, 1));
        }
    }
}
