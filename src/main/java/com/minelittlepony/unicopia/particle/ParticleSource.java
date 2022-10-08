package com.minelittlepony.unicopia.particle;

import java.util.function.Consumer;

import com.minelittlepony.unicopia.util.shape.PointGenerator;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface ParticleSource extends ParticleSpawner {

    /**
     * gets the minecraft world
     */
    World getReferenceWorld();

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

    default void spawnParticles(PointGenerator area, int count, Consumer<Vec3d> particleSpawner) {
        spawnParticles(getOriginVector(), area, count, particleSpawner);
    }

    default void spawnParticles(Vec3d pos, PointGenerator area, int count, Consumer<Vec3d> particleSpawner) {
        area.translate(pos).randomPoints(count, getReferenceWorld().random).forEach(particleSpawner);
    }

    @Override
    default void addParticle(ParticleEffect effect, Vec3d position, Vec3d velocity) {
        ParticleUtils.spawnParticle(getReferenceWorld(), effect, position, velocity);
    }
}
