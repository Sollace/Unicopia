package com.minelittlepony.unicopia.magic;

/**
 * A change listener for when a magic spell is either placed or destroyed in the world.
 */
public interface EtherialListener {
    int REMOVED = 0x0;
    int ADDED =   0x1;

    /**
     * Called when a nearby spell is added or removed.
     *
     * @param source The casting source of the sending spell
     * @param effect The spell that dispatched the event
     * @param state  The new state
     */
    void onNearbySpellChange(Caster<?> source, Spell effect, int state);
}
