package com.minelittlepony.unicopia.player;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.util.serialisation.InbtSerialisable;

import net.minecraft.entity.Entity;

/**
 * Generic container for an entity that has a race.
 *
 * @param <T> The type of owner
 */
public interface IRaceContainer<T extends Entity> extends InbtSerialisable, IUpdatable {
    Race getPlayerSpecies();

    void setPlayerSpecies(Race race);

    void onDimensionalTravel(int destinationDimension);

    /**
     * Called at the beginning of a player's update cycle.
     */
    void beforeUpdate();
}
