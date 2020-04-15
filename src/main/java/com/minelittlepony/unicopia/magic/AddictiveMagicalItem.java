package com.minelittlepony.unicopia.magic;

import com.minelittlepony.unicopia.entity.player.Pony;

public interface AddictiveMagicalItem extends MagicalItem {
    void onRemoved(Pony player, float needfulness);
}
