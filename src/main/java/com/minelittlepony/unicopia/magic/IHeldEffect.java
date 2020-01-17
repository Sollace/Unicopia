package com.minelittlepony.unicopia.magic;

import com.minelittlepony.unicopia.entity.capabilities.IPlayer;

/**
 * Represents a passive spell that does something when held in the player's hand.
 */
public interface IHeldEffect extends IMagicEffect {
    /**
     * Called every tick when held in a player's inventory.
     *
     * @param source    The entity we are currently attached to.
     */
    void updateInHand(IPlayer caster, Affinity affinity);
}
