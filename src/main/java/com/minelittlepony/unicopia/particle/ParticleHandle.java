package com.minelittlepony.unicopia.particle;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.ability.magic.Caster;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

/**
 * A connection class for updating and persisting an attached particle effect.
 */
public class ParticleHandle {
    private final Map<String, Attachment> loadedEffects = new WeakHashMap<>();

    public Optional<Attachment> update(UUID id, ParticleSource source, Consumer<ParticleSpawner> constructor) {
        return update(id, "prime", source, constructor);
    }

    public Optional<Attachment> update(UUID id, String partName, ParticleSource source, Consumer<ParticleSpawner> constructor) {
        return get(partName).or(() -> {
            if (source.getWorld().isClient) {
                new ClientHandle().addParticle(id, partName, source, constructor);
            }
            return get(partName);
        });
    }

    public void destroy() {
        loadedEffects.values().forEach(Attachment::detach);
        loadedEffects.clear();
    }

    private Optional<Attachment> get(String partName) {
        return Optional.ofNullable(loadedEffects.get(partName)).filter(Attachment::isStillAlive);
    }

    private final class ClientHandle {
        private static final Map<UUID, Map<String, Entry>> SPAWNED_PARTICLES = new HashMap<>();

        private Particle pp;

        @Environment(EnvType.CLIENT)
        private void addParticle(UUID id, String partName, ParticleSource source, Consumer<ParticleSpawner> constructor) {
            SPAWNED_PARTICLES.values().removeIf(set -> {
                set.values().removeIf(particle -> particle.get() == null);
                return set.isEmpty();
            });

            Entry p = SPAWNED_PARTICLES.computeIfAbsent(id, i -> new WeakHashMap<>()).computeIfAbsent(partName, i -> {
                constructor.accept((effect, pos, vel) -> {
                    pp = MinecraftClient.getInstance().particleManager.addParticle(effect, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
                    if (pp instanceof Attachment && source instanceof Caster<?>) {
                        ((Attachment) pp).attach(new Link(id, (Caster<?>)source));
                    }
                });
                return new Entry(new WeakReference<>(MinecraftClient.getInstance().world), new WeakReference<>(pp));
            });

            if (p.get() instanceof Attachment) {
                loadedEffects.put(partName, (Attachment)p.get());
            }
        }

        record Entry (WeakReference<World> world, WeakReference<Particle> particle) {
            public Particle get() {
                if (world.get() == null || world.get() != MinecraftClient.getInstance().world) {
                    return null;
                }

                Particle particle = this.particle.get();

                return particle == null || !particle.isAlive() ? null : particle;
            }
        }
    }

    public interface Attachment {
        boolean isStillAlive();

        void attach(Link link);

        void detach();

        void setAttribute(int key, Object value);
    }

    public static final class Link {
        private Optional<WeakReference<Caster<?>>> caster = Optional.empty();
        private UUID effect;

        private Link(UUID effect, Caster<?> caster) {
            this.caster = Optional.of(new WeakReference<>(caster));
            this.effect = effect;
        }

        public Optional<Caster<?>> get() {
            caster = caster.filter(r -> r.get() != null && r.get().getSpellSlot().contains(effect) && r.get().getEntity().isAlive());
            return caster.map(WeakReference::get);
        }
    }
}
