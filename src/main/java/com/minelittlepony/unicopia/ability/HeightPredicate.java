package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.equine.player.Pony;

/**
 * Predicate for abilities to control what the player's physical height is.
 *
 * This overrides the default.
 */
public interface HeightPredicate {
    float getTargetEyeHeight(Pony player);

    float getTargetBodyHeight(Pony player);
}
