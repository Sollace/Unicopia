package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.item.toxin.Toxics;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.TagKey;

public interface UTags {
    TagKey<Item> APPLES = item("apples");
    TagKey<Item> FRESH_APPLES = item("fresh_apples");

    TagKey<Item> FALLS_SLOWLY = item("falls_slowly");

    TagKey<Item> MAGIC_FEATHERS = item("magic_feathers");

    TagKey<Item> SHADES = item("shades");
    TagKey<Item> CHANGELING_EDIBLE = item("food_types/changeling_edible");

    TagKey<Item> POLEARMS = item("polearms");
    TagKey<Item> APPLE_SEEDS = item("apple_seeds");

    TagKey<Item> ACORNS = item("acorns");

    TagKey<Block> GLASS_PANES = block("glass_panes");
    TagKey<Block> GLASS_BLOCKS = block("glass_blocks");
    TagKey<Block> FRAGILE = block("fragile");
    TagKey<Block> INTERESTING = block("interesting");

    TagKey<Block> CRYSTAL_HEART_BASE = block("crystal_heart_base");
    TagKey<Block> CRYSTAL_HEART_ORNAMENT = block("crystal_heart_ornament");

    TagKey<Block> POLEARM_MINEABLE = block("mineable/polearm");

    TagKey<EntityType<?>> TRANSFORMABLE_ENTITIES = entity("transformable");

    TagKey<StatusEffect> PINEAPPLE_EFFECTS = effect("pineapple_effects");

    static TagKey<Item> item(String name) {
        return TagKey.of(RegistryKeys.ITEM, Unicopia.id(name));
    }

    static TagKey<Block> block(String name) {
        return TagKey.of(RegistryKeys.BLOCK, Unicopia.id(name));
    }

    static TagKey<EntityType<?>> entity(String name) {
        return TagKey.of(RegistryKeys.ENTITY_TYPE, Unicopia.id(name));
    }

    static TagKey<StatusEffect> effect(String name) {
        return TagKey.of(RegistryKeys.STATUS_EFFECT, Unicopia.id(name));
    }

    static void bootstrap() {
        Toxics.bootstrap();
    }
}
