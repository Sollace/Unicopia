package com.minelittlepony.unicopia.player;

/**
 * Predicate for abilities to control whether a player can fly.
 *
 * This overrides what the race specifies.
 */
public interface IFlyingPredicate {
    boolean checkCanFly(IPlayer player);
}