package com.minelittlepony.unicopia.particle;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.ability.magic.Caster;
import com.minelittlepony.unicopia.ability.magic.spell.Spell;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

/**
 * A connection class for updating and persisting an attached particle effect.
 */
public class ParticleHandle {

    private Optional<Attachment> particleEffect = Optional.empty();

    public Optional<Attachment> ifAbsent(ParticleSource source, Consumer<ParticleSpawner> constructor) {
        particleEffect.filter(Attachment::isStillAlive).orElseGet(() -> {
            if (source.getWorld().isClient) {
                constructor.accept(this::addParticle);
            }
            return null;
        });

        return particleEffect;
    }

    public void destroy() {
        particleEffect.ifPresent(Attachment::detach);
    }

    @Environment(EnvType.CLIENT)
    private void addParticle(ParticleEffect effect, Vec3d pos, Vec3d vel) {
        Particle p = MinecraftClient.getInstance().particleManager.addParticle(effect, pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);

        if (p instanceof Attachment) {
            particleEffect = Optional.of((Attachment)p);
        }
    }

    public interface Attachment {

        boolean isStillAlive();

        void attach(Caster<?> caster);

        void detach();

        void setAttribute(int key, Object value);
    }

    public static final class Link {

        private Optional<Caster<?>> caster = Optional.empty();
        private UUID effect;
        private boolean linked;

        public void attach(Caster<?> caster) {
            this.linked = true;
            this.caster = Optional.of(caster);
            this.effect = caster.getSpellSlot().get(false).map(Spell::getUuid).orElse(null);
        }

        public void detach() {
            caster = Optional.empty();
        }

        public boolean linked() {
            return linked;
        }

        public Optional<Caster<?>> ifAbsent(Runnable action) {
            caster = caster.filter(c -> {
                Entity e = c.getEntity();

                return Caster.of(e).orElse(null) == c
                        && c.getSpellSlot().get(false)
                            .filter(s -> s.getUuid().equals(effect))
                            .isPresent()
                        && e != null
                        && c.getWorld().getEntityById(e.getId()) != null;
            });
            if (!caster.isPresent()) {
                action.run();
            }

            return caster;
        }
    }
}
