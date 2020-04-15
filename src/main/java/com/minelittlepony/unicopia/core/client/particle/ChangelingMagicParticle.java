package com.minelittlepony.unicopia.core.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.world.World;

public class ChangelingMagicParticle extends MagicParticle {

    private final SpriteProvider provider;

    public ChangelingMagicParticle(SpriteProvider provider, World world, double x, double y, double z, double dx, double dy, double dz) {
        super(world, x, y, z, dx, dy, dz, 1, 1, 1);
        this.provider = provider;

        float intensity = random.nextFloat() * 0.6F + 0.4F;

        colorRed = intensity * 0.5F;
        colorGreen = intensity;
        colorBlue = intensity * 0.4f;
    }

    @Override
    public void tick() {
        setSpriteForAge(provider);

        super.tick();
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<DustParticleEffect> {
        private final SpriteProvider provider;

        public Factory(SpriteProvider provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(DustParticleEffect effect, World world, double x, double y, double z, double dx, double dy, double dz) {
            MagicParticle particle = new MagicParticle(world, x, y, z, dx, dy, dz);
            particle.setSprite(provider);
            return particle;
        }
    }
}
