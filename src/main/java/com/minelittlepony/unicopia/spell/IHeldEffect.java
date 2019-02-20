package com.minelittlepony.unicopia.spell;

import com.minelittlepony.unicopia.player.IPlayer;

public interface IHeldEffect extends IMagicEffect {
    /**
     * Called every tick when held in a player's inventory.
     *
     * @param source    The entity we are currently attached to.
     */
    void updateInHand(IPlayer caster, SpellAffinity affinity);
}
