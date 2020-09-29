package com.minelittlepony.unicopia.ability;

import java.util.Optional;

import com.minelittlepony.unicopia.entity.player.Pony;

import net.minecraft.entity.EntityDimensions;

/**
 * Predicate for abilities to control what the player's physical height is.
 *
 * This overrides the default.
 */
public interface DimensionsPredicate {
    float getTargetEyeHeight(Pony player);

    Optional<EntityDimensions> getTargetDimensions(Pony player);
}
