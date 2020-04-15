package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.RainSplashParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.world.World;

public class RaindropsParticle extends RainSplashParticle {

    public RaindropsParticle(World world, double x, double y, double z, double dx, double dy, double dz) {
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

    public static class Factory implements ParticleFactory<DefaultParticleType> {
        private final SpriteProvider provider;

        public Factory(SpriteProvider provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(DefaultParticleType defaultParticleType_1, World world, double x, double y, double z, double dx, double dy, double dz) {
            RaindropsParticle particle = new RaindropsParticle(world, x, y, z, dx, dy, dz);
            particle.setSprite(provider);
            return particle;
        }
    }
}
