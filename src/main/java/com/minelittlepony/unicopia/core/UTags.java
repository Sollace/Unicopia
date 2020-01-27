package com.minelittlepony.unicopia.core;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class UTags {
    // TODO: includes unicopia:alicorn_amulet
    public static final Tag<Item> CURSED_ARTEFACTS = TagRegistry.item(new Identifier(UnicopiaCore.MODID, "cursed_artefacts"));

    static void bootstrap() {}
}
