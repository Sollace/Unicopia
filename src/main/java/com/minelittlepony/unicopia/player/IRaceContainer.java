package com.minelittlepony.unicopia.player;

import com.minelittlepony.unicopia.InbtSerialisable;
import com.minelittlepony.unicopia.Race;

import net.minecraft.entity.Entity;

public interface IRaceContainer<T extends Entity> extends InbtSerialisable, IUpdatable<T> {
    Race getPlayerSpecies();

    void setPlayerSpecies(Race race);

}
