package com.minelittlepony.unicopia.entity.player;

import java.util.Optional;

import javax.annotation.Nullable;

import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.Spell;

import net.minecraft.entity.EntityDimensions;

public final class PlayerDimensions {

    private float defaultEyeHeight;

    @Nullable
    private EntityDimensions defaultDimensions;
    @Nullable
    private EntityDimensions flyingDimensions;

    private final PlayerPhysics physics;

    private final Pony pony;

    public PlayerDimensions(Pony pony, PlayerPhysics gravity) {
        this.pony = pony;
        this.physics = gravity;
    }

    public float calculateActiveEyeHeight(EntityDimensions dimensions, float original) {
        defaultEyeHeight = original;

        float height = calculateTargetEyeHeight();

        if (physics.isGravityNegative()) {
            if (pony.getMaster().isSneaking()) {
                height += 0.2F;
            }

            height = dimensions.height - height;
        }

        return height;
    }

    public EntityDimensions calculateDimensions(EntityDimensions dimensions) {
        if (defaultDimensions == null || dimensions.height != defaultDimensions.height || dimensions.width != defaultDimensions.width) {
            defaultDimensions = dimensions;
            flyingDimensions = EntityDimensions.changing(dimensions.width, dimensions.height / 2);
        }

        dimensions = getPredicate()
                .flatMap(e -> e.getTargetDimensions(pony))
                .orElseGet(() -> physics.isFlyingSurvival ? flyingDimensions : defaultDimensions);

        if (dimensions.height < 0 || dimensions.width < 0) {
            Unicopia.LOGGER.warn("Dim out was negative! Restoring original");
            return defaultDimensions;
        }

        return dimensions;
    }

    private float calculateTargetEyeHeight() {
        float height = getPredicate().map(e -> e.getTargetEyeHeight(pony)).orElse(-1F);

        if (height > 0) {
            return height;
        }

        return defaultEyeHeight;
    }

    Optional<Provider> getPredicate() {
        if (pony.hasSpell()) {
            Spell effect = pony.getSpell(true);
            if (!effect.isDead() && effect instanceof Provider) {
                return Optional.of(((Provider)effect));
            }
        }
        return Optional.empty();
    }

    public interface Provider {
        float getTargetEyeHeight(Pony player);

        Optional<EntityDimensions> getTargetDimensions(Pony player);
    }
}
