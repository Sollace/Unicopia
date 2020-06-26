package com.minelittlepony.unicopia.particles;

import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;

/**
 * Utility for spawning particles.
 */
public final class ParticleUtils {

    public static void spawnParticles(ParticleEffect particleId, Entity entity, int count) {
        double halfDist = Math.abs(entity.getStandingEyeHeight() / 1.5);
        double middle = entity.getBoundingBox().minY + halfDist;

        Shape shape = new Sphere(false, Math.abs((float)halfDist + entity.getWidth()));

        shape.randomPoints(count, entity.world.random).forEach(point -> {
            entity.world.addParticle(particleId,
                    entity.getX() + point.x,
                    middle + point.y,
                    entity.getZ() + point.z,
                    0, 0, 0);
        });
    }
}
