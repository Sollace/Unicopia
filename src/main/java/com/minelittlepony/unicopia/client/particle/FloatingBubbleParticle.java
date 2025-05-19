package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;

public class FloatingBubbleParticle extends SpriteBillboardParticle {
    public FloatingBubbleParticle(ParticleEffect effect, SpriteProvider provider, ClientWorld clientWorld, double x, double y, double z, double dX, double dY, double dZ) {
        super(clientWorld, x, y, z, dX, dY, dZ);
        setSprite(provider);
        scale((float)clientWorld.random.nextTriangular(1F, 0.5F));
        this.velocityX *= -0.1F;
        this.velocityY *= -0.1F;
        this.velocityZ *= -0.1F;
        this.maxAge *= 3;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void markDead() {
        super.markDead();
        world.addParticle(ParticleTypes.BUBBLE_POP, x, y, z, 0, 0, 0);
    }
}
