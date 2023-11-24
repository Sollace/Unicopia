package com.minelittlepony.unicopia.item.toxin;

import net.minecraft.item.Item;
import net.minecraft.registry.tag.TagKey;

@Deprecated
public record ToxicRegistryEntry (
        Toxic value,
        TagKey<Item> tag
    ) {

    public boolean matches(Item item) {
        return item.getRegistryEntry().isIn(tag);
    }
}
