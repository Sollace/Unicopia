package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.client.render.RenderLayers;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;
import com.minelittlepony.unicopia.util.ColorHelper;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Quaternion;

public class DiskParticle extends SphereParticle {

    private static final DiskModel MODEL = new DiskModel();

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
        if (colorAlpha <= 0 || radius <= 0) {
            return;
        }

        float[] color = ColorHelper.changeSaturation(colorRed, colorGreen, colorBlue, 4);
        RenderSystem.setShaderColor(color[0], color[1], color[2], colorAlpha / 3F);

        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer buffer = immediate.getBuffer(RenderLayers.getMagicGlow());

        int light = getBrightness(tickDelta);

        MatrixStack matrices = new MatrixStack();
        matrices.push();
        matrices.translate(x, y, z);
        matrices.multiply(new Quaternion(rotX, rotY, rotZ, true));
        matrices.scale(radius, radius, radius);
        MODEL.render(matrices, buffer, 1, light, 1, 1, 1, 1);

        matrices.pop();

        immediate.draw();

        RenderSystem.setShaderColor(1, 1, 1, 1);
    }
}

