package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3f;

public class ChangelingMagicParticle extends MagicParticle {

    private final SpriteProvider provider;

    public ChangelingMagicParticle(ParticleEffect effect, SpriteProvider provider, ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
        super(effect, provider, world, x, y, z, dx, dy, dz, nextColor(world.random.nextFloat() * 0.6F + 0.4F), 1);
        this.provider = provider;
    }

    static Vec3f nextColor(float intensity) {
        return new Vec3f(intensity * 0.5F, intensity, intensity * 0.4F);
    }

    @Override
    public void tick() {
        setSpriteForAge(provider);
        super.tick();
    }
}
