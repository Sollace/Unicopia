package com.minelittlepony.unicopia.datagen.providers.tag;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public interface SereneSeasonsTags {
    interface Items {
        TagKey<Item> AUTUMN_CROPS = item("autumn_crops");
        TagKey<Item> WINTER_CROPS = item("winter_crops");
        TagKey<Item> SPRING_CROPS = item("spring_crops");
        TagKey<Item> SUMMER_CROPS = item("summer_crops");

        private static TagKey<Item> item(String name) {
            return TagKey.of(RegistryKeys.ITEM, new Identifier("sereneseasons", name));
        }
    }

    interface Blocks {
        TagKey<Block> AUTUMN_CROPS = block("autumn_crops");
        TagKey<Block> WINTER_CROPS = block("winter_crops");
        TagKey<Block> SPRING_CROPS = block("spring_crops");
        TagKey<Block> SUMMER_CROPS = block("summer_crops");

        private static TagKey<Block> block(String name) {
            return TagKey.of(RegistryKeys.BLOCK, new Identifier("sereneseasons", name));
        }
    }

}
