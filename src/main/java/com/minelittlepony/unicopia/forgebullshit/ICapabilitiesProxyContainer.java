package com.minelittlepony.unicopia.forgebullshit;

import com.minelittlepony.unicopia.player.IPlayer;
import com.minelittlepony.unicopia.player.IRaceContainer;
import com.minelittlepony.unicopia.util.serialisation.InbtSerialisable;

import net.minecraft.entity.Entity;

public interface ICapabilitiesProxyContainer<T extends Entity> extends InbtSerialisable {
    IPlayer getPlayer();

    IRaceContainer<T> getRaceContainer();

    ICapabilitiesProxyContainer<T> withEntity(T entity);
}
