package com.minelittlepony.unicopia.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.Vec3f;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class AbstractBillboardParticle extends Particle {

    protected float scale = 1;

    public AbstractBillboardParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.CUSTOM;
    }

    @Override
    public void buildGeometry(VertexConsumer drawer, Camera camera, float tickDelta) {
        Tessellator te = Tessellator.getInstance();
        BufferBuilder buffer = te.getBuffer();

        bindTexture(getTexture());

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA
        );

        Vec3d cam = camera.getPos();

        float renderX = (float)(MathHelper.lerp(tickDelta, prevPosX, x) - cam.getX());
        float renderY = (float)(MathHelper.lerp(tickDelta, prevPosY, y) - cam.getY());
        float renderZ = (float)(MathHelper.lerp(tickDelta, prevPosZ, z) - cam.getZ());

        renderQuads(te, buffer, renderX, renderY, renderZ, tickDelta);

        RenderSystem.enableCull();
        RenderSystem.defaultBlendFunc();
    }

    protected abstract void renderQuads(Tessellator te, BufferBuilder buffer, float x, float y, float z, float tickDelta);

    protected void bindTexture(Identifier texture) {
        RenderSystem.setShaderTexture(0, texture);
    }

    protected void renderQuad(Tessellator te, BufferBuilder buffer, Vec3f[] corners, float alpha, float tickDelta) {
        int light = getBrightness(tickDelta);

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        buffer.vertex(corners[0].getX(), corners[0].getY(), corners[0].getZ()).texture(0, 0).color(colorRed, colorGreen, colorBlue, alpha).light(light).next();
        buffer.vertex(corners[1].getX(), corners[1].getY(), corners[1].getZ()).texture(1, 0).color(colorRed, colorGreen, colorBlue, alpha).light(light).next();
        buffer.vertex(corners[2].getX(), corners[2].getY(), corners[2].getZ()).texture(1, 1).color(colorRed, colorGreen, colorBlue, alpha).light(light).next();
        buffer.vertex(corners[3].getX(), corners[3].getY(), corners[3].getZ()).texture(0, 1).color(colorRed, colorGreen, colorBlue, alpha).light(light).next();

        te.draw();
    }

    protected abstract Identifier getTexture();

    public float getScale(float tickDelta) {
       return scale;
    }

    @Override
    public Particle scale(float scale) {
       this.scale = scale;
       return super.scale(scale);
    }
}
