package com.minelittlepony.unicopia.client.particle;

import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;

public class ChangelingMagicParticle extends MagicParticle {

    private final SpriteProvider provider;

    public ChangelingMagicParticle(SpriteProvider provider, ClientWorld world, double x, double y, double z, double dx, double dy, double dz) {
        super(provider, world, x, y, z, dx, dy, dz, 1, 1, 1);
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
}
