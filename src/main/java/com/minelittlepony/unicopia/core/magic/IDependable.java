package com.minelittlepony.unicopia.core.magic;

import com.minelittlepony.unicopia.core.entity.player.IPlayer;

public interface IDependable extends IMagicalItem {
    void onRemoved(IPlayer player, float needfulness);
}
