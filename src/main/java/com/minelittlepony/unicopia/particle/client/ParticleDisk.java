package com.minelittlepony.unicopia.particle.client;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

import com.minelittlepony.unicopia.model.ModelDisk;
import com.minelittlepony.util.render.Color;

public class ParticleDisk extends ParticleSphere {

    private static final ModelDisk model = new ModelDisk();

    protected float rotX;
    protected float rotY;
    protected float rotZ;

    public ParticleDisk(int id, World w, double x, double y, double z, double vX, double vY, double vZ, int... args) {
        this(w, x, y, z, args[0]/255F, args[1]/255F, args[2]/255F, args[3], args[4], args[5]/255F);
    }

    public ParticleDisk(World w, double x, double y, double z, float rX, float rY, float rZ, int radius, int tint, float alpha) {
        super(w, x, y, z, radius, tint, alpha);

        rotX = rX;
        rotY = rY;
        rotZ = rZ;
    }

    public void renderParticle(BufferBuilder buffer, Entity viewer, float partialTicks, float x, float z, float yz, float xy, float xz) {
        if (alpha <= 0) {
            return;
        }

        Color.glColor(tint, alpha);

        model.setPosition(posX, posY, posZ);
        model.render(radius);

        GlStateManager.color(1, 1, 1, 1);
    }
}

