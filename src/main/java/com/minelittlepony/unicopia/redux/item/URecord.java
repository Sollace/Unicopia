package com.minelittlepony.unicopia.redux.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Rarity;
public class URecord extends MusicDiscItem {

    public URecord(SoundEvent sound) {
        super(1, sound, new Item.Settings()
            .maxCount(1)
            .group(ItemGroup.MISC)
            .rarity(Rarity.RARE)
        );
    }

}
