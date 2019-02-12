package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.Race;

/**
 * This interface is for any entities that are categorised as inanimated,
 * or part of a terrain effect.
 *
 * These typically can't be interacted with by players unless under certain cirumstances.
 *
 */
public interface IInAnimate {
    boolean canInteract(Race race);
}
