package com.minelittlepony.unicopia.ability;

import com.minelittlepony.unicopia.entity.player.IPlayer;

/**
 * Predicate for abilities to control whether a player can fly.
 *
 * This overrides what the race specifies.
 */
public interface IFlyingPredicate {
    boolean checkCanFly(IPlayer player);
}