package com.minelittlepony.unicopia.particle;

import java.util.function.Consumer;

import com.minelittlepony.unicopia.util.shape.Shape;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ParticleSource extends ParticleSpawner {

    /**
     * gets the minecraft world
     */
    World getWorld();

    Entity getEntity();

    /**
     * Gets the center position where this caster is located.
     */
    default Vec3d getOriginVector() {
        return getEntity().getPos();
    }

    default void spawnParticles(ParticleEffect particleId, int count) {
        ParticleUtils.spawnParticles(particleId, getEntity(), count);
    }

    default void spawnParticles(Shape area, int count, Consumer<Vec3d> particleSpawner) {
        spawnParticles(getOriginVector(), area, count, particleSpawner);
    }

    default void spawnParticles(Vec3d pos, Shape area, int count, Consumer<Vec3d> particleSpawner) {
        area.randomPoints(count, getWorld().random)
            .map(point -> point.add(pos))
            .forEach(particleSpawner);
    }


    @Override
    default void addParticle(ParticleEffect effect, Vec3d position, Vec3d velocity) {
        getWorld().addParticle(effect, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
    }

}
