package com.minelittlepony.unicopia;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public interface UTags {
    Tag<Item> APPLES = item("apples");
    Tag<Item> FESH_APPLES = item("fresh_apples");

    Tag<Item> NON_TOXIC = item("non_toxic");
    Tag<Item> FAIRLY_TOXIC = item("fairly_toxic");
    Tag<Item> SEVERELY_TOXIC = item("severely_toxic");

    Tag<Block> FRAGILE = block("fragile");
    Tag<Block> INTERESTING = block("interesting");

    static Tag<Item> item(String name) {
        return TagRegistry.item(new Identifier("unicopia", name));
    }

    static Tag<Block> block(String name) {
        return TagRegistry.block(new Identifier("unicopia", name));
    }

    static void bootstrap() { }
}
