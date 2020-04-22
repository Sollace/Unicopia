package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.client.render.model.DiskModel;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.World;

public class DiskParticle extends SphereParticle {

    private static final DiskModel model = new DiskModel();

    protected float rotX;
    protected float rotY;
    protected float rotZ;

    public DiskParticle(World w,
            double x, double y, double z,
            float radius,
            int red, int green, int blue, float alpha,
            float rX, float rY, float rZ) {
        super(w, x, y, z, radius, red, green, blue, alpha);

        rotX = rX;
        rotY = rY;
        rotZ = rZ;
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        if (alpha <= 0) {
            return;
        }

        MatrixStack matrices = new MatrixStack();
        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        model.setPosition(x, y, z);
        model.setRotation(rotX, rotY, rotZ);
        model.render(matrices, radius, immediate.getBuffer(RenderLayer.getTranslucent()), 1, 1, red, green, blue, alpha);
        immediate.draw();
    }
}

