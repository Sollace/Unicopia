package com.minelittlepony.unicopia;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public interface UTags {
    Tag<Item> CURSED_ARTEFACTS = TagRegistry.item(new Identifier(Unicopia.MODID, "cursed_artefacts"));
    Tag<Item> HAMMERPACE_IMMUNE = TagRegistry.item(new Identifier(Unicopia.MODID, "hammerspace_immune"));

    static void bootstrap() { }
}
