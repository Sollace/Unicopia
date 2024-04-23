package com.minelittlepony.unicopia.client.particle;

import org.jetbrains.annotations.Nullable;

import com.minelittlepony.unicopia.particle.SpiralParticleEffect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class SpiralParticle extends NoRenderParticle {

    private final SpiralParticleEffect parameters;

    @Nullable
    private final Particle particle;

    private float scale;

    public SpiralParticle(SpiralParticleEffect parameters, SpriteProvider provider, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        super(world, x, y, z, velocityX, velocityY, velocityZ);
        this.scale = 0.1f * (random.nextFloat() * 0.5f + 0.5f) * 2.0f;
        setMaxAge(3);
        scale(0.125F);
        this.parameters = parameters;
        this.collidesWithWorld = false;
        this.particle = MinecraftClient.getInstance().particleManager.addParticle(parameters.effect(), x, y, z, velocityX, velocityY, velocityZ);
        this.particle.setMaxAge(1000);
        this.gravityStrength = 0;
    }

    @Override
    public Particle scale(float scale) {
        this.scale *= scale;
        super.scale(scale);
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
        if (particle == null || !particle.isAlive()) {
            markDead();
        }

        super.tick();

        Vec3d target = parameters.centerPoint().getPosition(world);
        Vec3d pos = new Vec3d(x, y, z);

        if (scale * 1.5F < 0.5F) {
            scale(1.5F);
        }

        double distance = pos.distanceTo(target);
        if (distance > 0) {
            age = 0;
        }

        Vec3d radial = target.subtract(pos).normalize();
        Vec3d tangent = radial.rotateY(MathHelper.HALF_PI).multiply(parameters.angularVelocity() * 0.9F);
        Vec3d motion = radial.multiply(parameters.angularVelocity() * 0.1F).add(tangent);
        move(motion.x, motion.y, motion.z);
    }

    @Override
    public String toString() {
        return super.toString() + ", Angular Velocity " + parameters.angularVelocity() + ", Target (" + parameters.centerPoint() + ") Sub-Particle (" + particle + ")";
    }
}
