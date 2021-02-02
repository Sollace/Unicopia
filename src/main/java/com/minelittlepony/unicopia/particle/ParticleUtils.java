package com.minelittlepony.unicopia.particle;

import com.minelittlepony.unicopia.util.shape.Shape;
import com.minelittlepony.unicopia.util.shape.Sphere;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * Utility for spawning particles.
 */
public final class ParticleUtils {

    public static void spawnParticles(ParticleEffect particleId, Entity entity, int count) {
        double halfDist = Math.abs(entity.getStandingEyeHeight() / 1.5);
        double middle = entity.getBoundingBox().minY + halfDist;

        Shape shape = new Sphere(false, Math.abs((float)halfDist + entity.getWidth()));

        shape.randomPoints(count, entity.world.random).forEach(point -> {
            spawnParticle(entity.world, particleId,
                    entity.getX() + point.x,
                    middle + point.y,
                    entity.getZ() + point.z,
                    0, 0, 0);
        });
    }

    public static void spawnParticle(World world, ParticleEffect effect, double x, double y, double z, double vX, double vY, double vZ) {
        if (world instanceof ServerWorld) {
            ((ServerWorld)world).spawnParticles(effect, x, y, z, 1, vX, vY, vZ, 0);
        } else {
            world.addParticle(effect, x, y, z, vX, vY, vZ);
        }

    }
}
