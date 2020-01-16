package com.minelittlepony.unicopia.particles;

import com.minelittlepony.util.shape.IShape;
import com.minelittlepony.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;

/**
 * Utility interface for spawning particles.
 */
public final class ParticleUtils {

    public static void spawnParticles(ParticleEffect particleId, Entity entity, int count) {
        double halfDist = entity.getStandingEyeHeight() / 1.5;
        double middle = entity.getBoundingBox().minY + halfDist;

        IShape shape = new Sphere(false, (float)halfDist + entity.getWidth());

        shape.randomPoints(count, entity.world.random).forEach(point -> {
            entity.world.addParticle(particleId,
                    entity.x + point.x,
                    middle + point.y,
                    entity.z + point.z,
                    0, 0, 0);
        });
    }
}
