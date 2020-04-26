package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particles.MagicParticleEffect;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.world.World;

public class MagicParticle extends SpriteBillboardParticle {
    private double startX;
    private double startY;
    private double startZ;

    MagicParticle(World w, double x, double y, double z, double vX, double vY, double vZ, float r, float g, float b) {
        super(w, x, y, z);
        velocityX = vX;
        velocityY = vY;
        velocityZ = vZ;
        startX = x + random.nextGaussian()/3;
        startY = y + random.nextGaussian()/3;
        startZ = z + random.nextGaussian()/3;
        scale = random.nextFloat() * 0.12F;
        maxAge = (int)(Math.random() * 10) + 20;

        colorRed = r;
        colorGreen = g;
        colorBlue = b;
    }

    MagicParticle(World w, double x, double y, double z, double vX, double vY, double vZ) {
        this(w, x, y, z, vX, vY, vZ, 1, 1, 1);

        colorAlpha = 0.7F;
        colorGreen *= 0.3F;

        if (random.nextBoolean()) {
            colorBlue *= 0.4F;
        }
        if (random.nextBoolean()) {
            colorRed *= 0.9F;
        }
        if (random.nextBoolean()) {
            colorGreen += 0.5F;
        }

        if (random.nextBoolean()) {
            colorGreen *= 2F;
        } else if (random.nextBoolean()) {
            colorRed *= 3.9F;
        }
    }

    @Override
    public ParticleTextureSheet getType() {
       return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public float getSize(float tickDelta) {
       float shrink = (age + tickDelta) / maxAge;
       return this.scale * (1 - shrink * shrink);
    }

    @Override
    public int getColorMultiplier(float p_70070_1_) {
        int i = super.getColorMultiplier(p_70070_1_);
        float f1 = (float)age / (float)maxAge;
        f1 *= f1;
        f1 *= f1;
        int j = i & 255;
        int k = i >> 16 & 255;
        k += f1 * 15 * 16;
        if (k > 240) k = 240;
        return j | k << 16;
    }

    @Override
    public void tick() {
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
        } else {
            float var1 = (float)age / (float)maxAge;
            var1 = 1 + var1 - var1 * var1 * 2;

            x = startX + velocityX * var1;
            y = startY + velocityY;
            z = startZ + velocityZ * var1;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<MagicParticleEffect> {
        private final FabricSpriteProvider provider;

        public Factory(FabricSpriteProvider provider) {
            this.provider = provider;
        }

        @Override
        public Particle createParticle(MagicParticleEffect effect, World world, double x, double y, double z, double dx, double dy, double dz) {
            MagicParticle particle = effect.hasTint() ?
                    new MagicParticle(world, x, y, z, dx, dy, dz, effect.getRed(), effect.getGreen(), effect.getBlue())
                     : new MagicParticle(world, x, y, z, dx, dy, dz);
            particle.setSprite(provider);
            return particle;
        }
    }
}
