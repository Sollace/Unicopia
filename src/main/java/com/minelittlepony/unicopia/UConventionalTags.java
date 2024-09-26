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
        TagKey<Block> GLAZED_TERRACOTTAS = block("glazed_terracottas");
        TagKey<Block> CORAL_BLOCKS = block("coral_blocks");
        TagKey<Block> CORAL_FANS = block("coral_fans");
        TagKey<Block> CORALS = block("corals");

        private static TagKey<Block> block(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of("c", name));
        }
    }

    interface Items {
        TagKey<Item> CONCRETE_POWDERS = item("concrete_powders");
        TagKey<Item> CONCRETES = item("concretes");
        TagKey<Item> GLAZED_TERRACOTTAS = item("glazed_terracottas");
        TagKey<Item> CORAL_BLOCKS = item("coral_blocks");
        TagKey<Item> CORAL_FANS = item("coral_fans");
        TagKey<Item> CORALS = item("corals");

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
        TagKey<Item> COOKIES = item("cookies");

        TagKey<Item> FRUITS = item("fruits");
        TagKey<Item> WORMS = item("worms");
        TagKey<Item> ROCKS = item("rocks");
        TagKey<Item> GEMS = item("gems");

        TagKey<Item> RAW_INSECT = item("raw_insect");
        TagKey<Item> COOKED_INSECT = item("cooked_insect");
        TagKey<Item> ROTTEN_INSECT = item("rotten_insect");

        TagKey<Item> RAW_FISH = item("raw_fish");
        TagKey<Item> COOKED_FISH = item("cooked_fish");
        TagKey<Item> ROTTEN_FISH = item("rotten_fish");
        TagKey<Item> RAW_MEAT = item("raw_meat");
        TagKey<Item> COOKED_MEAT = item("cooked_meat");
        TagKey<Item> ROTTEN_MEAT = item("rotten_meat");
        TagKey<Item> DESSERTS = item("desserts");
        TagKey<Item> CANDY = item("candy");

        TagKey<Item> CROPS_PEANUTS = item("crops/peanuts");
        TagKey<Item> TOOL_KNIVES = item("tools/knives");

        private static TagKey<Item> item(String name) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of("c", name));
        }
    }
}
