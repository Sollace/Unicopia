package com.minelittlepony.unicopia.client.particle;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.client.render.RenderUtil;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;

public abstract class AbstractGeometryBasedParticle extends Particle {

    protected float scale = 1;

    public AbstractGeometryBasedParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    protected final void renderQuad(MatrixStack matrices, VertexConsumer buffer, RenderUtil.Vertex[] corners, float alpha, float tickDelta) {
        int light = getBrightness(tickDelta);
        for (RenderUtil.Vertex corner : corners) {
            var position = corner.position(matrices.peek().getPositionMatrix());
            buffer.vertex(position.x, position.y, position.z).texture(corner.texture().x, corner.texture().y).color(red, green, blue, alpha).light(light);
        }
    }

    protected final void renderQuad(VertexConsumer buffer, RenderUtil.Vertex[] corners, float alpha, float tickDelta) {
        quad(buffer, corners, alpha, tickDelta, getBrightness(tickDelta));
    }

    protected final void quad(VertexConsumer buffer, RenderUtil.Vertex[] corners, float alpha, float tickDelta, int light) {
        for (RenderUtil.Vertex corner : corners) {
            buffer.vertex(corner.position().x, corner.position().y, corner.position().z).texture(corner.texture().x, corner.texture().y).color(red, green, blue, alpha).light(light);
        }
    }

    protected final void renderQuad(VertexConsumer buffer, Vector3f[] corners, float alpha, float tickDelta) {
        int light = getBrightness(tickDelta);
        for (Vector3f corner : corners) {
            buffer.vertex(corner.x, corner.y, corner.z).color(red, green, blue, alpha).light(light);
        }
    }

    public float getScale(float tickDelta) {
       return scale;
    }

    @Override
    public Particle scale(float scale) {
       this.scale = scale;
       return super.scale(scale);
    }
}
