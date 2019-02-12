package com.minelittlepony.unicopia.player;

/**
 * Predicate for abilities to control what the player's physical height is.
 *
 * This overrides the default.
 */
public interface IPlayerHeightPredicate {
    float getTargetEyeHeight(IPlayer player);

    float getTargetBodyHeight(IPlayer player);
}
