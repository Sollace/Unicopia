package com.minelittlepony.unicopia;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public interface UTags {
    Tag<Item> APPLES = register("apples");
    Tag<Item> FESH_APPLES = register("fresh_apples");

    Tag<Item> NON_TOXIC = register("non_toxic");
    Tag<Item> FAIRLY_TOXIC = register("fairly_toxic");
    Tag<Item> SEVERELY_TOXIC = register("severely_toxic");

    Tag<Block> FRAGILE = TagRegistry.block(new Identifier("unicopia", "fragile"));

    static Tag<Item> register(String name) {
        return TagRegistry.item(new Identifier("unicopia", name));
    }

    static void bootstrap() { }
}
