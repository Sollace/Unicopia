package com.minelittlepony.unicopia.particle.client;

import net.minecraft.client.particle.ParticlePortal;
import net.minecraft.world.World;

public class ParticleChanglingMagic extends ParticlePortal {

    private int baseTextureIndex = 128;

    public ParticleChanglingMagic(int id, World w, double x, double y, double z, double vX, double vY, double vZ, int... args) {
        this(w, x, y, z, vX, vY, vZ);
    }

    public ParticleChanglingMagic(World world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z, dx, dy, dz);

        float intensity = rand.nextFloat() * 0.6F + 0.4F;

        particleRed = intensity * 0.5F;
        particleGreen = intensity;
        particleBlue = intensity * 0.4f;

        setParticleTextureIndex((int)(Math.random() * 8.0D));
    }

    @Override
    public void onUpdate() {
        setParticleTextureIndex(baseTextureIndex + (7 - particleAge * 8 / particleMaxAge));

        super.onUpdate();
    }

}
