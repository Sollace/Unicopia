package com.minelittlepony.unicopia.client.particle;

import org.joml.Vector3f;

import com.minelittlepony.unicopia.client.render.RenderUtil;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
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

    protected final void renderQuad(Tessellator te, BufferBuilder buffer, Vector3f[] corners, float alpha, float tickDelta) {
        int light = getBrightness(tickDelta);

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        buffer.vertex(corners[0].x, corners[0].y, corners[0].z).texture(0, 0).color(red, green, blue, alpha).light(light).next();
        buffer.vertex(corners[1].x, corners[1].y, corners[1].z).texture(1, 0).color(red, green, blue, alpha).light(light).next();
        buffer.vertex(corners[2].x, corners[2].y, corners[2].z).texture(1, 1).color(red, green, blue, alpha).light(light).next();
        buffer.vertex(corners[3].x, corners[3].y, corners[3].z).texture(0, 1).color(red, green, blue, alpha).light(light).next();

        te.draw();
    }

    protected final void renderQuad(Tessellator te, BufferBuilder buffer, RenderUtil.Vertex[] corners, float alpha, float tickDelta) {
        int light = getBrightness(tickDelta);
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        for (RenderUtil.Vertex corner : corners) {
            buffer.vertex(corner.position().x, corner.position().y, corner.position().z).texture(corner.texture().x, corner.texture().y).color(red, green, blue, alpha).light(light).next();
        }
        te.draw();
    }


    protected final void renderQuad(VertexConsumer buffer, Vector3f[] corners, float alpha, float tickDelta) {
        int light = getBrightness(tickDelta);

        for (Vector3f corner : corners) {
            buffer.vertex(corner.x, corner.y, corner.z).color(red, green, blue, alpha).light(light).next();
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
