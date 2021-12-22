package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.item.toxin.Toxics;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public interface UTags {
    Tag<Item> APPLES = item("apples");
    Tag<Item> FRESH_APPLES = item("fresh_apples");

    Tag<Item> FALLS_SLOWLY = item("falls_slowly");

    Tag<Item> MAGIC_FEATHERS = item("magic_feathers");

    Tag<Item> SHADES = item("shades");

    Tag<Block> FRAGILE = block("fragile");
    Tag<Block> INTERESTING = block("interesting");

    Tag<Block> CRYSTAL_HEART_BASE = block("crystal_heart_base");
    Tag<Block> CRYSTAL_HEART_ORNAMENT = block("crystal_heart_ornament");

    Tag<EntityType<?>> TRANSFORMABLE_ENTITIES = entity("transformable");

    static Tag<Item> item(String name) {
        return TagFactory.ITEM.create(new Identifier("unicopia", name));
    }

    static Tag<Block> block(String name) {
        return TagFactory.BLOCK.create(new Identifier("unicopia", name));
    }

    static Tag<EntityType<?>> entity(String name) {
        return TagFactory.ENTITY_TYPE.create(new Identifier("unicopia", name));
    }

    static void bootstrap() {
        Toxics.bootstrap();
    }
}
