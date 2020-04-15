package com.minelittlepony.unicopia.util.particles;

import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;

/**
 * Utility for spawning particles.
 */
public final class ParticleUtils {

    public static void spawnParticles(ParticleEffect particleId, Entity entity, int count) {
        double halfDist = entity.getStandingEyeHeight() / 1.5;
        double middle = entity.getBoundingBox().minY + halfDist;

        Shape shape = new Sphere(false, (float)halfDist + entity.getWidth());

        shape.randomPoints(count, entity.world.random).forEach(point -> {
            entity.world.addParticle(particleId,
                    entity.x + point.x,
                    middle + point.y,
                    entity.z + point.z,
                    0, 0, 0);
        });
    }
}
