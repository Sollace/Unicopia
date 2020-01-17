package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.particle.RainSplashParticle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.world.World;

public class ParticleRaindrops extends RainSplashParticle {

    public ParticleRaindrops(ParticleEffect type, World world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z);
        velocityY = -0.1;
        maxAge += 19;
    }

    @Override
    public void tick() {
        super.tick();

        if (onGround) {
            velocityX *= 0.30000001192092896D;
            velocityY = Math.random() * 0.20000000298023224D + 0.10000000149011612D;
            velocityZ *= 0.30000001192092896D;
        }
    }
}