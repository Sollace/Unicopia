package com.minelittlepony.unicopia.particle;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.EntityConvertable;
import com.minelittlepony.unicopia.util.shape.PointGenerator;

import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

public interface ParticleSource<E extends Entity> extends ParticleSpawner, EntityConvertable<E> {

    default void spawnParticles(ParticleEffect particleId, Supplier<Vec3d> posSupplier, Supplier<Vec3d> velSupplier, int count) {
        for (int i = 0; i < count; i++) {
            spawnParticle(particleId, posSupplier, velSupplier);
        }
    }

    default void spawnParticle(ParticleEffect particleId, Supplier<Vec3d> posSupplier, Supplier<Vec3d> velSupplier) {
        addParticle(particleId, getOriginVector().add(posSupplier.get()), velSupplier.get());
    }

    default void spawnParticles(ParticleEffect particleId, int count) {
        ParticleUtils.spawnParticles(particleId, asEntity(), count);
    }

    default void spawnParticles(PointGenerator area, int count, Consumer<Vec3d> particleSpawner) {
        spawnParticles(getOriginVector(), area, count, particleSpawner);
    }

    default void spawnParticles(Vec3d pos, PointGenerator area, int count, Consumer<Vec3d> particleSpawner) {
        area.translate(pos).randomPoints(count, asWorld().random).forEach(particleSpawner);
    }

    @Override
    default void addParticle(ParticleEffect effect, Vec3d position, Vec3d velocity) {
        ParticleUtils.spawnParticle(asWorld(), effect, position, velocity);
    }
}
