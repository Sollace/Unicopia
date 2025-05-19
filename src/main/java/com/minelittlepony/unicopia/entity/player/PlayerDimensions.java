package com.minelittlepony.unicopia.entity.player;

import java.util.Optional;

import net.minecraft.entity.EntityDimensions;

public final class PlayerDimensions {
    private static final float FLYING_HEIGHT = 0.6F;
    private static final Optional<EntityDimensions> FLYING_DIMENSIONS = Optional.of(
            EntityDimensions.changing(FLYING_HEIGHT, FLYING_HEIGHT).withEyeHeight(FLYING_HEIGHT * 0.6F)
    );

    private final PlayerPhysics physics;

    private final Pony pony;

    public PlayerDimensions(Pony pony, PlayerPhysics gravity) {
        this.pony = pony;
        this.physics = gravity;
    }

    public EntityDimensions calculateDimensions(EntityDimensions original) {
        EntityDimensions dimensions = getPredicate()
                .flatMap(e -> e.getTargetDimensions(pony))
                 .or(() -> physics.isFlyingSurvival ? FLYING_DIMENSIONS : Optional.empty())
                .filter(d -> d.height() > 0 && d.width() > 0)
                .orElse(original);

        if (physics.isGravityNegative()) {
            return dimensions.withEyeHeight(dimensions.eyeHeight() - original.eyeHeight() + 0.1F);
        }

        return dimensions;
    }

    Optional<Provider> getPredicate() {
        return pony.getSpellSlot().get()
                .filter(effect -> !effect.isDead() && effect instanceof Provider)
                .map(effect -> (Provider)effect);
    }

    public interface Provider {
        Optional<EntityDimensions> getTargetDimensions(Pony player);
    }
}
