package com.minelittlepony.unicopia.particles;

import java.util.Optional;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.magic.Caster;

/**
 * A connection class for updating and persisting an attached particle effect.
 */
public class ParticleConnection {

    private Optional<AttachableParticle> particleEffect = Optional.empty();

    public Optional<AttachableParticle> ifMissing(Caster<?> source, Supplier<Optional<AttachableParticle>> constructor) {
        particleEffect.filter(AttachableParticle::isStillAlive).orElseGet(() -> {
            particleEffect = constructor.get();
            particleEffect.ifPresent(p -> p.attachTo(source));
            return null;
        });

        return particleEffect;
    }

    public interface AttachableParticle {

        boolean isStillAlive();

        void attachTo(Caster<?> caster);

        void setAttribute(int key, Object value);
    }
}
