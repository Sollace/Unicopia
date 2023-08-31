package com.minelittlepony.unicopia.particle;

import com.minelittlepony.unicopia.util.shape.*;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Utility for spawning particles.
 */
public interface ParticleUtils {

    static PointGenerator getShapeFor(Entity entity) {
        final double halfDist = Math.abs(entity.getStandingEyeHeight() / 1.5);
        final double middle = entity.getBoundingBox().minY + halfDist;
        return new Sphere(false, Math.abs((float)halfDist + entity.getWidth())).translate(new Vec3d(entity.getX(), middle, entity.getZ()));
    }

    static void spawnParticles(ParticleEffect effect, Entity entity, int count) {
        spawnParticles(entity.getWorld(), getShapeFor(entity), effect, count);
    }

    static void spawnParticles(ParticleEffect effect, World world, Vec3d origin, int count) {
        spawnParticles(world, Sphere.UNIT_SPHERE.translate(origin), effect, count);
    }

    static void spawnParticles(World world, PointGenerator points, ParticleEffect effect, int count) {
        points.randomPoints(count, world.random).forEach(point -> spawnParticle(world, effect, point, Vec3d.ZERO));
    }

    static void spawnParticle(World world, ParticleEffect effect, Vec3d pos, Vec3d vel) {
        spawnParticle(world, effect, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
    }

    static void spawnParticle(World world, ParticleEffect effect, double x, double y, double z, double vX, double vY, double vZ) {
        if (world instanceof ServerWorld sw) {
            Vec3d vel = new Vec3d(vX, vY, vZ);

            sw.spawnParticles(effect, x, y, z, 1, vX, vY, vZ, vel.length());
        } else {
            world.addParticle(effect, x, y, z, vX, vY, vZ);
        }
    }
}
