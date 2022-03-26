package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3f;
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

    protected final void renderQuad(Tessellator te, BufferBuilder buffer, Vec3f[] corners, float alpha, float tickDelta) {
        int light = getBrightness(tickDelta);

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        buffer.vertex(corners[0].getX(), corners[0].getY(), corners[0].getZ()).texture(0, 0).color(red, green, blue, alpha).light(light).next();
        buffer.vertex(corners[1].getX(), corners[1].getY(), corners[1].getZ()).texture(1, 0).color(red, green, blue, alpha).light(light).next();
        buffer.vertex(corners[2].getX(), corners[2].getY(), corners[2].getZ()).texture(1, 1).color(red, green, blue, alpha).light(light).next();
        buffer.vertex(corners[3].getX(), corners[3].getY(), corners[3].getZ()).texture(0, 1).color(red, green, blue, alpha).light(light).next();

        te.draw();
    }

    protected final void renderQuad(VertexConsumer buffer, Vec3f[] corners, float alpha, float tickDelta) {
        int light = getBrightness(tickDelta);

        for (Vec3f corner : corners) {
            buffer.vertex(corner.getX(), corner.getY(), corner.getZ()).color(red, green, blue, alpha).light(light).next();
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
