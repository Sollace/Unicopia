package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.FollowingParticleEffect;

import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

public class HealthDrainParticle extends SpriteBillboardParticle {

    private final FollowingParticleEffect effect;

    public HealthDrainParticle(FollowingParticleEffect effect, SpriteProvider provider, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        setSprite(provider);
        setMaxAge(3);
        scale(0.125F);
        this.effect = effect;
        this.collidesWithWorld = false;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();

        Vec3d target = effect.getTarget(world);
        Vec3d pos = new Vec3d(x, y, z);

        if (this.scale * 1.5F < 0.5F) {
            scale(1.5F);
        }

        double distance = pos.distanceTo(target);
        if (distance > 1) {
            age = 0;
        }

        Vec3d motion = target.subtract(pos).normalize().multiply(Math.min(distance, effect.getSpeed()));
        move(motion.x, motion.y, motion.z);
    }
}
