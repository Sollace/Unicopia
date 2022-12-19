package com.minelittlepony.unicopia.entity.player;

import java.util.Optional;

import net.minecraft.entity.EntityDimensions;

public final class PlayerDimensions {
    private static final float FLYING_HEIGHT = 0.6F;
    private static final Optional<EntityDimensions> FLYING_DIMENSIONS = Optional.of(EntityDimensions.changing(FLYING_HEIGHT, FLYING_HEIGHT));
    private static final Optional<Float> FLYING_EYE_HEIGHT = Optional.of(FLYING_HEIGHT * 0.6F);

    private final PlayerPhysics physics;

    private final Pony pony;

    public PlayerDimensions(Pony pony, PlayerPhysics gravity) {
        this.pony = pony;
        this.physics = gravity;
    }

    public Optional<Float> calculateActiveEyeHeight(EntityDimensions dimensions) {
        return getPredicate()
                .flatMap(e -> e.getTargetEyeHeight(pony))
                .filter(h -> h > 0)
                .or(() -> physics.isFlyingSurvival ? FLYING_EYE_HEIGHT : physics.isGravityNegative() ? Optional.of(dimensions.height) : Optional.empty())
                .map(h -> {
                    if (physics.isGravityNegative()) {
                        if (pony.asEntity().isSneaking()) {
                            h += 0.2F;
                        }

                        return dimensions.height - h;
                    }
                    return h;
                });
    }

    public Optional<EntityDimensions> calculateDimensions() {
        return getPredicate()
                .flatMap(e -> e.getTargetDimensions(pony))
                 .or(() -> physics.isFlyingSurvival ? FLYING_DIMENSIONS : Optional.empty())
                .filter(d -> d.height > 0 && d.width > 0);
    }

    Optional<Provider> getPredicate() {
        return pony.getSpellSlot().get(true)
                .filter(effect -> !effect.isDead() && effect instanceof Provider)
                .map(effect -> (Provider)effect);
    }

    public interface Provider {
        Optional<Float> getTargetEyeHeight(Pony player);

        Optional<EntityDimensions> getTargetDimensions(Pony player);
    }
}
