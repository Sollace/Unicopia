package com.minelittlepony.unicopia.particle;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.minelittlepony.unicopia.UClient;
import com.minelittlepony.unicopia.particle.client.ParticlesClient;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Particles<Particle> {

    private static Particles<?> instance;

    @SuppressWarnings("unchecked")
    public static <T> Particles<T> instance() {
        if (instance == null) {
            if (UClient.isClientSide()) {
                instance = new ParticlesClient();
            } else {
                instance = new Particles<>();
            }
        }
        return (Particles<T>)instance;
    }

    protected final List<IFactory<Particle>> registeredParticles = new ArrayList<>();

    private final EntityParticleEmitter entityEmitter = new EntityParticleEmitter();

    public EntityParticleEmitter getEntityEmitter() {
        return entityEmitter;
    }

    public int registerParticle(IFactory<Particle> factory) {
        int id = registeredParticles.size();
        registeredParticles.add(factory);
        return -id - 1;
    }

    @Nullable
    public Optional<IAttachableParticle> spawnParticle(int particleId, boolean ignoreDistance, Vec3d pos, Vec3d vel, int ...pars) {
        return spawnParticle(particleId, ignoreDistance, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z, pars);
    }

    @Nullable
    public Optional<IAttachableParticle> spawnParticle(int particleId, boolean ignoreDistance, Vec3d pos, double speedX, double speedY, double speedZ, int ...pars) {
        return spawnParticle(particleId, ignoreDistance, pos.x, pos.y, pos.z, speedX, speedY, speedZ, pars);
    }

    @Nullable
    public Optional<IAttachableParticle> spawnParticle(int particleId, boolean ignoreDistance, double posX, double posY, double posZ, double speedX, double speedY, double speedZ, int ...pars) {
        /* noop */
        /* or network */
        /* i don't know */
        /* ... */
        /* maybe later */

        return Optional.empty();
    }

    public static interface IFactory<T> {
        @Nonnull
        T createParticle(int id, World world, double x, double y, double z, double dx, double dy, double dz, int... args);
    }
}
