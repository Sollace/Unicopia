package com.minelittlepony.unicopia.item;

import com.minelittlepony.unicopia.player.IPlayer;

public interface IDependable extends IMagicalItem {
    void onRemoved(IPlayer player, float needfulness);
}
