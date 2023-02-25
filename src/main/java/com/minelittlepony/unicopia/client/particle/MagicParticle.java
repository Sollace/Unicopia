package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.MagicParticleEffect;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3f;

public class MagicParticle extends SpriteBillboardParticle {
    private final double startX;
    private final double startY;
    private final double startZ;

    MagicParticle(ParticleEffect effect, SpriteProvider provider, ClientWorld w, double x, double y, double z, double vX, double vY, double vZ, Vec3f color, float alpha) {
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

        red = color.getX();
        green = color.getY();
        blue = color.getZ();
        this.alpha = alpha;
    }

    public MagicParticle(MagicParticleEffect effect, SpriteProvider provider, ClientWorld w, double x, double y, double z, double vX, double vY, double vZ) {
        this(effect, provider, w, x, y, z, vX, vY, vZ, effect.getColor(w.random), 0.7F);
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
        v = (int)Math.min(v + (timer * timer * timer * timer) * 240, 240);

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

            timer += 1 - (timer * timer) * 2;

            x = startX + velocityX * timer;
            y = startY + velocityY;
            z = startZ + velocityZ * timer;
        }
    }

}
