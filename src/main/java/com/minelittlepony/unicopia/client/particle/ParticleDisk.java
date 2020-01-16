package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;

import com.minelittlepony.unicopia.client.render.model.DiskModel;
import com.mojang.blaze3d.platform.GlStateManager;

public class ParticleDisk extends ParticleSphere {

    private static final DiskModel model = new DiskModel();

    protected float rotX;
    protected float rotY;
    protected float rotZ;

    public ParticleDisk(ParticleEffect type, World w, double x, double y, double z, double rX, double rY, double rZ) {
        super(w, x, y, z, radius, tint, alpha);

        rotX = rX;
        rotY = rY;
        rotZ = rZ;
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks, float x, float z, float yz, float xy, float xz) {
        if (alpha <= 0) {
            return;
        }

        Color.glColor(tint, alpha);

        model.setPosition(this.x, this.y, this.z);
        model.render(radius);

        GlStateManager.color(1, 1, 1, 1);
    }
}

