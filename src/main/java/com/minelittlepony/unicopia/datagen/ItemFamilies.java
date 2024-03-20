package com.minelittlepony.unicopia.datagen;

import com.minelittlepony.unicopia.item.UItems;

import net.minecraft.item.Item;

public interface ItemFamilies {
    Item[] MUSIC_DISCS = {
            UItems.MUSIC_DISC_CRUSADE, UItems.MUSIC_DISC_FUNK, UItems.MUSIC_DISC_PET, UItems.MUSIC_DISC_POPULAR
    };
    Item[] POLEARMS = {
            UItems.WOODEN_POLEARM, UItems.STONE_POLEARM, UItems.IRON_POLEARM, UItems.GOLDEN_POLEARM, UItems.DIAMOND_POLEARM, UItems.NETHERITE_POLEARM
    };
    Item[] HORSE_SHOES = {
            UItems.IRON_HORSE_SHOE, UItems.GOLDEN_HORSE_SHOE, UItems.COPPER_HORSE_SHOE, UItems.NETHERITE_HORSE_SHOE
    };
    Item[] BASKETS = {
            UItems.ACACIA_BASKET, UItems.BAMBOO_BASKET, UItems.BIRCH_BASKET, UItems.CHERRY_BASKET, UItems.DARK_OAK_BASKET,
            UItems.JUNGLE_BASKET, UItems.MANGROVE_BASKET, UItems.OAK_BASKET, UItems.PALM_BASKET, UItems.SPRUCE_BASKET
    };
}
