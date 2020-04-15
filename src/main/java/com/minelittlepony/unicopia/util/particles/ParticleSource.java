package com.minelittlepony.unicopia.util.particles;

import java.util.function.Consumer;

import com.minelittlepony.unicopia.util.shape.Shape;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ParticleSource {

    /**
     * gets the minecraft world
     */
    World getWorld();

    /**
     * Gets the center position where this caster is located.
     */
    Vec3d getOriginVector();

    Entity getEntity();

    default void spawnParticles(ParticleEffect particleId, int count) {
        ParticleUtils.spawnParticles(particleId, getEntity(), count);
    }

    default void spawnParticles(Shape area, int count, Consumer<Vec3d> particleSpawner) {
        Vec3d pos = getOriginVector();

        area.randomPoints(count, getWorld().random).stream()
            .map(point -> point.add(pos))
            .forEach(particleSpawner);
    }

    default void addParticle(ParticleEffect effect, Vec3d position, Vec3d velocity) {
        getWorld().addParticle(effect, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
    }

}
