package com.minelittlepony.unicopia.magic.items;

import com.minelittlepony.unicopia.entity.capabilities.IPlayer;

public interface IDependable extends IMagicalItem {
    void onRemoved(IPlayer player, float needfulness);
}
