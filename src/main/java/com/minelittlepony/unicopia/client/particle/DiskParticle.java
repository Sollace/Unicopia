package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.SphereParticleEffect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;

public class DiskParticle extends SphereParticle {

    private static final DiskModel model = new DiskModel();

    protected float rotX;
    protected float rotY;
    protected float rotZ;

    public DiskParticle(SphereParticleEffect effect, ClientWorld w, double x, double y, double z, double rX, double rY, double rZ) {
        super(effect, w, x, y, z, 0, 0, 0);

        rotX = (float)rX;
        rotY = (float)rY;
        rotZ = (float)rZ;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        if (colorAlpha <= 0) {
            return;
        }

        MatrixStack matrices = new MatrixStack();
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        model.setPosition(x, y, z);
        model.setRotation(rotX, rotY, rotZ);
        model.render(matrices, radius, immediate.getBuffer(RenderLayer.getTranslucent()), 1, 1, colorRed, colorGreen, colorBlue, colorAlpha);
        immediate.draw();
    }
}

