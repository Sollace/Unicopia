package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.item.toxin.Toxics;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public interface UTags {
    Tag<Item> APPLES = item("apples");
    Tag<Item> FESH_APPLES = item("fresh_apples");

    Tag<Item> FALLS_SLOWLY = item("falls_slowly");

    Tag<Item> MAGIC_FEATHERS = item("magic_feathers");

    Tag<Item> SHADES = item("shades");

    Tag<Block> FRAGILE = block("fragile");
    Tag<Block> INTERESTING = block("interesting");

    Tag<Block> CRYSTAL_HEART_BASE = block("crystal_heart_base");
    Tag<Block> CRYSTAL_HEART_ORNAMENT = block("crystal_heart_ornament");

    static Tag<Item> item(String name) {
        return TagRegistry.item(new Identifier("unicopia", name));
    }

    static Tag<Block> block(String name) {
        return TagRegistry.block(new Identifier("unicopia", name));
    }

    static void bootstrap() {
        Toxics.bootstrap();
    }
}
