package com.minelittlepony.unicopia;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface UConventionalTags {
    interface Blocks {
        TagKey<Block> CONCRETE_POWDERS = block("concrete_powders");
        TagKey<Block> CONCRETES = block("concretes");

        private static TagKey<Block> block(String name) {
            return TagKey.of(RegistryKeys.BLOCK, new Identifier("c", name));
        }
    }

    interface Items {
        TagKey<Item> CONCRETE_POWDERS = item("concrete_powders");
        TagKey<Item> CONCRETES = item("concretes");

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
        TagKey<Item> OATMEALS = item("oatmeals");

        TagKey<Item> FRUITS = item("fruits");

        TagKey<Item> COOKED_FISH = item("cooked_fish");

        TagKey<Item> CROPS_PEANUTS = item("crops/peanuts");
        TagKey<Item> TOOL_KNIVES = item("tools/knives");

        private static TagKey<Item> item(String name) {
            return TagKey.of(RegistryKeys.ITEM, new Identifier("c", name));
        }
    }
}
