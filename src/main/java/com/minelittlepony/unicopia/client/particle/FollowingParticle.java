package com.minelittlepony.unicopia.client.particle;

import com.minelittlepony.unicopia.particle.FollowingParticleEffect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;

public class FollowingParticle extends NoRenderParticle {

    private final FollowingParticleEffect parameters;

    private final Particle particle;

    private float scale;

    public FollowingParticle(FollowingParticleEffect parameters, SpriteProvider provider, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.scale = 0.1f * (this.random.nextFloat() * 0.5f + 0.5f) * 2.0f;
        setMaxAge(3);
        scale(0.125F);
        this.parameters = parameters;
        this.collidesWithWorld = false;
        this.particle = parameters.getChildEffect().map(child -> MinecraftClient.getInstance().particleManager.addParticle(child, x, y, z, velocityX, velocityY, velocityZ)).orElse(null);
    }

    @Override
    public Particle scale(float scale) {
        this.scale *= scale;
        super.scale(scale);
        if (particle != null) {
            particle.scale(scale);
        }
        return this;
    }

    @Override
    public void move(double dx, double dy, double dz) {
        super.move(dx, dy, dz);
        if (particle != null) {
            particle.setPos(x, y, z);
        }
    }

    @Override
    public void tick() {
        if (this.particle == null || !this.particle.isAlive()) {
            markDead();
        }

        super.tick();

        Vec3d target = parameters.getTarget(world);
        Vec3d pos = new Vec3d(x, y, z);

        if (scale * 1.5F < 0.5F) {
            scale(1.5F);
        }

        double distance = pos.distanceTo(target);
        if (distance > 1) {
            age = 0;
        }

        Vec3d motion = target.subtract(pos).normalize().multiply(Math.min(distance, parameters.getSpeed()));
        move(motion.x, motion.y, motion.z);
    }

    @Override
    public String toString() {
        return super.toString() + ", Speed " + parameters.getSpeed() + ", Target (" + parameters.getTargetDescriptor() + ") Sub-Particle (" + particle + ")";
    }
}
