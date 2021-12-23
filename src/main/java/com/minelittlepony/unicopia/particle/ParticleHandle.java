package com.minelittlepony.unicopia.particle;

import java.lang.ref.WeakReference;
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
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

/**
 * A connection class for updating and persisting an attached particle effect.
 */
public class ParticleHandle {
    private Optional<WeakReference<Attachment>> particleEffect = Optional.empty();

    public Optional<Attachment> ifAbsent(UUID id, ParticleSource source, Consumer<ParticleSpawner> constructor) {
        return get().or(() -> {
            if (source.getWorld().isClient) {
                constructor.accept((effect, pos, vel) -> new ClientHandle().addParticle(id, source, effect, pos, vel));
            }
            return get();
        });
    }

    public void destroy() {
        get().ifPresent(Attachment::detach);
    }

    private Optional<Attachment> get() {
        return particleEffect.map(WeakReference::get).filter(Attachment::isStillAlive);
    }

    private final class ClientHandle {
        private static final Map<UUID, Particle> SPAWNED_PARTICLES = new WeakHashMap<>();

        @Environment(EnvType.CLIENT)
        private void addParticle(UUID id, ParticleSource source, ParticleEffect effect, Vec3d pos, Vec3d vel) {
            Particle p = SPAWNED_PARTICLES.computeIfAbsent(id, i -> {
                Particle pp = MinecraftClient.getInstance().particleManager.addParticle(effect, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
                if (pp instanceof Attachment && source instanceof Caster<?>) {
                    ((Attachment) pp).attach(new Link(id, (Caster<?>)source));
                }
                return pp;
            });

            if (p instanceof Attachment) {
                particleEffect = Optional.of(new WeakReference<>((Attachment)p));
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
            caster = caster.filter(r -> r.get() != null && r.get().getSpellSlot().contains(effect));
            return caster.map(WeakReference::get);
        }
    }
}
