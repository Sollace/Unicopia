package com.minelittlepony.unicopia.magic;

import com.minelittlepony.unicopia.entity.player.Pony;

/**
 * Represents a passive spell that does something when held in the player's hand.
 */
public interface HeldSpell extends Spell {
    /**
     * Called every tick when held in a player's inventory.
     *
     * @param source    The entity we are currently attached to.
     */
    void updateInHand(Pony caster, Affinity affinity);
}
