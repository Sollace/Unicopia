package com.minelittlepony.unicopia.magic.item;

import com.minelittlepony.unicopia.entity.player.Pony;

/**
 * A magical item with addictive properties.
 */
public interface AddictiveMagicitem extends MagicItem {
    void onRemoved(Pony player, float needfulness);
}
