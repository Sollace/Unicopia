package com.minelittlepony.unicopia.client.particle;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import com.minelittlepony.unicopia.particle.ParticleSpawner;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * A connection class for updating and persisting an attached particle effect.
 */
public class ClientBoundParticleSpawner implements ParticleSpawner {
    private static final Map<UUID, Entry> SPAWNED_PARTICLES = new HashMap<>();

    private final UUID id;
    private WeakReference<BooleanSupplier> attachment = new WeakReference<>(null);

    private final MinecraftClient client = MinecraftClient.getInstance();

    public ClientBoundParticleSpawner(UUID id) {
        this.id = id;
    }

    @Override
    public void addParticle(ParticleEffect effect, Vec3d pos, Vec3d vel) {
        BooleanSupplier a = attachment.get();
        if ((a == null || !a.getAsBoolean())) {
            SPAWNED_PARTICLES.values().removeIf(set -> !set.getAsBoolean());
            attachment = new WeakReference<>(SPAWNED_PARTICLES.computeIfAbsent(id, i -> {
                return new Entry(
                        new WeakReference<>(client.world),
                        new WeakReference<>(client.particleManager.addParticle(effect, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z))
                );
            }));
        }
    }

    private record Entry (WeakReference<World> world, WeakReference<Particle> particle) implements BooleanSupplier {
        @Override
        public boolean getAsBoolean() {
            if (world.get() == null || world.get() != MinecraftClient.getInstance().world) {
                return false;
            }

            Particle particle = this.particle.get();
            return particle != null && particle.isAlive();
        }
    }
}
