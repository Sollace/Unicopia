package com.minelittlepony.unicopia.entity;

import com.minelittlepony.unicopia.magic.items.IDependable;
import com.minelittlepony.unicopia.magic.items.IMagicalItem;

public interface IInventory {

    /**
     * Reinforces a players dependency on a certain magical artifact.
     * A dependency will slowly drop over time if not reinforced
     *
     * Bad things might happen when it's removed.
     */
    void enforceDependency(IDependable item);

    /**
     * Checks if the player is wearing the specified magical artifact.
     */
    boolean isWearing(IMagicalItem item);

    /**
     * Returns how long the player has been wearing the given item.
     */
    int getTicksAttached(IDependable item);

    /**
     * Returns how dependent the player has become on the given item.
     *
     * Zero means not dependent at all / not wearing.
     */
    float getNeedfulness(IDependable item);
}
