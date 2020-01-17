package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.entity.capabilities.IPlayer;

/**
 * Predicate for abilities to control what the player's physical height is.
 *
 * This overrides the default.
 */
public interface IHeightPredicate {
    float getTargetEyeHeight(IPlayer player);

    float getTargetBodyHeight(IPlayer player);
}
