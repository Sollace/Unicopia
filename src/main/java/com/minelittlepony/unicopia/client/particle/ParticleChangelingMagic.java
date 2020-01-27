package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.PortalParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;

public class ParticleChangelingMagic extends PortalParticle {

    private int baseTextureIndex = 128;

    public ParticleChangelingMagic(World world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z, dx, dy, dz);

        float intensity = rand.nextFloat() * 0.6F + 0.4F;

        colorRed = intensity * 0.5F;
        colorGreen = intensity;
        colorBlue = intensity * 0.4f;

        setParticleTextureIndex((int)(Math.random() * 8.0D));
    }

    @Override
    public void tick() {
        setParticleTextureIndex(baseTextureIndex + (7 - age * 8 / maxAge));

        super.tick();
    }
}
