package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.SphereParticleEffect;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Quaternion;

public class DiskParticle extends SphereParticle {

    private final Quaternion rotation;

    public DiskParticle(SphereParticleEffect effect, ClientWorld w, double x, double y, double z, double rX, double rY, double rZ) {
        super(effect, w, x, y, z, 0, 0, 0);
        rotation = new Quaternion((float)rX, (float)rY, (float)rZ, true);
    }

    @Override
    protected void renderModel(MatrixStack matrices, VertexConsumer buffer, float scale, float tickDelta, int light) {
        matrices.multiply(rotation);
        SphereModel.DISK.render(matrices, buffer, light, 1, scale, 1, 1, 1, 1);
    }
}

