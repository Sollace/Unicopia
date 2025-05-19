package com.minelittlepony.unicopia.client.particle;

import org.joml.Quaternionf;

import com.minelittlepony.unicopia.client.render.model.SphereModel;
import com.minelittlepony.unicopia.particle.SphereParticleEffect;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Colors;
import net.minecraft.util.math.*;

public class DiskParticle extends SphereParticle {

    private final Quaternionf rotation = new Quaternionf(0, 0, 0, 1);

    public DiskParticle(SphereParticleEffect effect, ClientWorld w, double x, double y, double z, double rX, double rY, double rZ) {
        super(effect.withOffset(new Vec3d(0, 0.25, 0)), w, x, y, z, 0, 0, 0);

        rotation.mul(RotationAxis.POSITIVE_Y.rotationDegrees((float)effect.offset().y));
        rotation.mul(RotationAxis.POSITIVE_X.rotationDegrees(180 - (float)effect.offset().x));
    }

    @Override
    protected void renderModel(MatrixStack matrices, VertexConsumer buffer, float scale, float tickDelta, int light) {
        matrices.multiply(rotation);
        float thickness = 0.2F;
        SphereModel.DISK.render(matrices, buffer, light, 1, scale, Colors.WHITE);
        matrices.translate(0, -thickness, 0);
        SphereModel.DISK.render(matrices, buffer, light, 1, scale, Colors.WHITE);
    }
}

