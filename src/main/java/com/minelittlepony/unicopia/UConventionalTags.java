package com.minelittlepony.unicopia;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface UConventionalTags {
    TagKey<Item> APPLES = item("apples");
    TagKey<Item> ACORNS = item("acorns");
    TagKey<Item> PINECONES = item("pinecones");
    TagKey<Item> PINEAPPLES = item("pineapples");
    TagKey<Item> BANANAS = item("bananas");
    TagKey<Item> STICKS = item("sticks");
    TagKey<Item> SEEDS = item("seeds");
    TagKey<Item> GRAIN = item("grain");
    TagKey<Item> NUTS = item("nuts");
    TagKey<Item> MUSHROOMS = item("mushrooms");
    TagKey<Item> MUFFINS = item("muffins");
    TagKey<Item> MANGOES = item("mangoes");
    TagKey<Item> OEATMEALS = item("oatmeals");

    TagKey<Item> FRUITS = item("fruits");

    TagKey<Item> COOKED_FISH = item("cooked_fish");

    TagKey<Item> CROPS_PEANUTS = item("crops/peanuts");
    TagKey<Item> TOOL_KNIVES = item("tools/knives");

    static TagKey<Item> item(String name) {
        return TagKey.of(RegistryKeys.ITEM, new Identifier("c", name));
    }

    static TagKey<Block> block(String name) {
        return TagKey.of(RegistryKeys.BLOCK, new Identifier("c", name));
    }
}
