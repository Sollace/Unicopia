package com.minelittlepony.unicopia.item.toxin;

import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;

public record ToxicRegistryEntry (
        Toxic value,
        TagKey<Item> tag
    ) {

    @SuppressWarnings("deprecation")
    public boolean matches(Item item) {
        return item.getRegistryEntry().isIn(tag);
    }

}
