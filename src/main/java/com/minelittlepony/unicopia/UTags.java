package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.item.toxin.Toxics;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tag.TagKey;
import net.minecraft.util.registry.Registry;

public interface UTags {
    TagKey<Item> APPLES = item("apples");
    TagKey<Item> FRESH_APPLES = item("fresh_apples");

    TagKey<Item> FALLS_SLOWLY = item("falls_slowly");

    TagKey<Item> MAGIC_FEATHERS = item("magic_feathers");

    TagKey<Item> SHADES = item("shades");

    TagKey<Block> FRAGILE = block("fragile");
    TagKey<Block> INTERESTING = block("interesting");

    TagKey<Block> CRYSTAL_HEART_BASE = block("crystal_heart_base");
    TagKey<Block> CRYSTAL_HEART_ORNAMENT = block("crystal_heart_ornament");

    TagKey<EntityType<?>> TRANSFORMABLE_ENTITIES = entity("transformable");

    static TagKey<Item> item(String name) {
        return TagKey.of(Registry.ITEM_KEY, Unicopia.id(name));
    }

    static TagKey<Block> block(String name) {
        return TagKey.of(Registry.BLOCK_KEY, Unicopia.id(name));
    }

    static TagKey<EntityType<?>> entity(String name) {
        return TagKey.of(Registry.ENTITY_TYPE_KEY, Unicopia.id(name));
    }

    static void bootstrap() {
        Toxics.bootstrap();
    }
}
