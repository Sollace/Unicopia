package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.MagicParticleEffect;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;

public class MagicParticle extends SpriteBillboardParticle {
    private double startX;
    private double startY;
    private double startZ;

    MagicParticle(ParticleEffect effect, SpriteProvider provider, ClientWorld w, double x, double y, double z, double vX, double vY, double vZ, float r, float g, float b) {
        super(w, x, y, z);
        setSprite(provider);

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

    public MagicParticle(ParticleEffect effect, SpriteProvider provider, ClientWorld w, double x, double y, double z, double vX, double vY, double vZ) {
        this(effect, provider, w, x, y, z, vX, vY, vZ, 1, 1, 1);

        colorAlpha = 0.7F;
        colorGreen *= 0.3F;

        if (effect instanceof MagicParticleEffect && ((MagicParticleEffect)effect).hasTint()) {
            MagicParticleEffect parameters = (MagicParticleEffect)effect;

            colorRed = parameters.getColor().getX();
            colorGreen = parameters.getColor().getY();
            colorBlue = parameters.getColor().getZ();
        } else {

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
    public int getBrightness(float tint) {
        int light = super.getBrightness(tint);
        float timer = (float)age / (float)maxAge;

        int v = light >> 16 & 255;
        v = (int)Math.min(v + Math.pow(timer, 4) * 240, 240);

        return (light & 255) | v << 16;
    }

    @Override
    public void tick() {
        prevPosX = x;
        prevPosY = y;
        prevPosZ = z;

        if (age++ >= maxAge) {
            markDead();
        } else {
            float timer = (float)age / (float)maxAge;
            timer = 1 + timer - timer * timer * 2;

            x = startX + velocityX * timer;
            y = startY + velocityY;
            z = startZ + velocityZ * timer;
        }
    }

}
