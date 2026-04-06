package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
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
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        Vec3d cam = camera.getPos();

        float renderX = (float)(MathHelper.lerp(tickDelta, lastX, x) - cam.getX());
        float renderY = (float)(MathHelper.lerp(tickDelta, lastY, y) - cam.getY());
        float renderZ = (float)(MathHelper.lerp(tickDelta, lastZ, z) - cam.getZ());

        var immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        renderQuads(immediate.getBuffer(RenderLayer.getTranslucentParticle(getTexture())), renderX, renderY, renderZ, tickDelta);
        immediate.draw();
    }

    protected abstract void renderQuads(VertexConsumer vertexConsumer, float x, float y, float z, float tickDelta);

    protected abstract Identifier getTexture();
}
