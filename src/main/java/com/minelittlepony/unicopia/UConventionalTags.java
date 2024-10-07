package com.minelittlepony.unicopia;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface UConventionalTags {
    interface Blocks {
        TagKey<Block> CONCRETE_POWDERS = block("concrete_powders");

        TagKey<Block> CORAL_BLOCKS = block("coral_blocks");
        TagKey<Block> CORAL_FANS = block("coral_fans");
        TagKey<Block> CORALS = block("corals");

        private static TagKey<Block> block(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of("c", name));
        }
    }

    interface Items {
        TagKey<Item> CONCRETES = item("concretes");
        TagKey<Item> CORAL_BLOCKS = item("coral_blocks");
        TagKey<Item> CORAL_FANS = item("coral_fans");
        TagKey<Item> CORALS = item("corals");

        TagKey<Item> APPLES = item("foods/apple");
        TagKey<Item> ACORNS = item("foods/acorn");
        TagKey<Item> PINECONES = item("foods/pinecone");
        TagKey<Item> PINEAPPLES = item("foods/pineapple");
        TagKey<Item> BANANAS = item("foods/banana");

        TagKey<Item> SEEDS = item("seeds");
        TagKey<Item> GRAIN = item("grain");
        TagKey<Item> NUTS = item("nuts");
        TagKey<Item> MUSHROOMS = item("foods/mushroom");
        TagKey<Item> MUFFINS = item("foods/muffin");
        TagKey<Item> MANGOES = item("foods/mango");
        TagKey<Item> OATMEALS = item("foods/oatmeal");
        TagKey<Item> COOKIES = item("foods/cookie");

        TagKey<Item> WORMS = item("worms");
        TagKey<Item> ROCKS = item("rocks");
        TagKey<Item> GEMS = item("gems");

        TagKey<Item> RAW_INSECT = item("foods/raw_insect");
        TagKey<Item> COOKED_INSECT = item("foods/cooked_insect");
        TagKey<Item> ROTTEN_INSECT = item("foods/rotten_insect");

        TagKey<Item> ROTTEN_FISH = item("foods/rotten_fish");

        TagKey<Item> ROTTEN_MEAT = item("foods/rotten_meat");
        TagKey<Item> DESSERTS = item("foods/dessert");

        TagKey<Item> CROPS_PEANUTS = item("crops/peanuts");
        TagKey<Item> TOOL_KNIVES = item("tools/knives");

        private static TagKey<Item> item(String name) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of("c", name));
        }
    }
}
