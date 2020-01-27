package com.minelittlepony.unicopia.core.magic;

import java.util.function.Consumer;

import com.minelittlepony.unicopia.core.util.shape.IShape;

import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface IParticleSource {

    /**
     * gets the minecraft world
     */
    World getWorld();

    /**
     * Gets the center position where this caster is located.
     */
    Vec3d getOriginVector();

    default void spawnParticles(ParticleEffect particleId, int count) {
        // TODO:
        // ParticleTypeRegistry.getInstance().getSpawner().spawnParticles(particleId, getEntity(), count);
    }

    default void spawnParticles(IShape area, int count, Consumer<Vec3d> particleSpawner) {
        Vec3d pos = getOriginVector();

        area.randomPoints(count, getWorld().random).stream()
            .map(point -> point.add(pos))
            .forEach(particleSpawner);
    }

    default void addParticle(ParticleEffect effect, Vec3d position, Vec3d velocity) {
        getWorld().addParticle(effect, position.x, position.y, position.z, velocity.x, velocity.y, velocity.z);
    }

}
