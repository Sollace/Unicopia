package com.minelittlepony.unicopia.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public abstract class AbstractBillboardParticle extends AbstractGeometryBasedParticle {

    public AbstractBillboardParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
    }

    @Override
    public void buildGeometry(VertexConsumer drawer, Camera camera, float tickDelta) {
        Tessellator te = Tessellator.getInstance();
        BufferBuilder buffer = te.getBuffer();

        RenderSystem.setShaderTexture(0, getTexture());

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();

        Vec3d cam = camera.getPos();

        float renderX = (float)(MathHelper.lerp(tickDelta, prevPosX, x) - cam.getX());
        float renderY = (float)(MathHelper.lerp(tickDelta, prevPosY, y) - cam.getY());
        float renderZ = (float)(MathHelper.lerp(tickDelta, prevPosZ, z) - cam.getZ());

        renderQuads(te, buffer, renderX, renderY, renderZ, tickDelta);

        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
    }

    protected abstract void renderQuads(Tessellator te, BufferBuilder buffer, float x, float y, float z, float tickDelta);

    protected abstract Identifier getTexture();
}
