package com.minelittlepony.unicopia.core.util.particles;

import java.util.Optional;
import java.util.function.Supplier;

import com.minelittlepony.unicopia.core.magic.ICaster;

/**
 * A connection class for updating and persisting an attached particle effect.
 */
public class ParticleConnection {

    private Optional<IAttachableParticle> particleEffect = Optional.empty();

    public Optional<IAttachableParticle> ifMissing(ICaster<?> source, Supplier<Optional<IAttachableParticle>> constructor) {
        particleEffect.filter(IAttachableParticle::isStillAlive).orElseGet(() -> {
            particleEffect = constructor.get();
            particleEffect.ifPresent(p -> p.attachTo(source));
            return null;
        });

        return particleEffect;
    }

    public interface IAttachableParticle {

        boolean isStillAlive();

        void attachTo(ICaster<?> caster);

        void setAttribute(int key, Object value);
    }
}
