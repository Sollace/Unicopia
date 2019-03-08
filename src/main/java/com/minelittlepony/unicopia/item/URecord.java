package com.minelittlepony.unicopia.item;

import net.minecraft.item.ItemRecord;
import net.minecraft.util.SoundEvent;

public class URecord extends ItemRecord {

    public URecord(String domain, String name, SoundEvent sound) {
        super(name, sound);
        setTranslationKey("record");
        setRegistryName(domain, "record_" + name);
    }

}
