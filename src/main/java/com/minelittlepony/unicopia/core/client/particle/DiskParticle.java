package com.minelittlepony.unicopia.core.client.particle;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL14;

import com.minelittlepony.unicopia.core.client.render.DiskModel;
import com.mojang.blaze3d.platform.GlStateManager;

public class DiskParticle extends SphereParticle {

    private static final DiskModel model = new DiskModel();

    protected double rotX;
    protected double rotY;
    protected double rotZ;

    public DiskParticle(ParticleEffect type, World w,
            double x, double y, double z,
            float radius,
            int red, int green, int blue, float alpha,
            double rX, double rY, double rZ) {
        super(w, x, y, z, radius, red, green, blue, alpha);

        rotX = rX;
        rotY = rY;
        rotZ = rZ;
    }

    @Override
    public void buildGeometry(BufferBuilder buffer, Camera viewer, float partialTicks, float x, float z, float yz, float xy, float xz) {
        if (alpha <= 0) {
            return;
        }

        GL14.glBlendColor(red, green, blue, alpha);

        model.setPosition(this.x, this.y, this.z);
        model.render(radius);

        GlStateManager.color4f(1, 1, 1, 1);
    }
}

