package com.minelittlepony.unicopia.magic.items;

import com.minelittlepony.unicopia.entity.player.IPlayer;

public interface IDependable extends IMagicalItem {
    void onRemoved(IPlayer player, float needfulness);
}
