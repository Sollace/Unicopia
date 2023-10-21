package com.minelittlepony.unicopia;

import com.minelittlepony.unicopia.item.toxin.Toxics;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.registry.*;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

public interface UTags {
    TagKey<Item> APPLES = item("apples");
    TagKey<Item> FRESH_APPLES = item("fresh_apples");

    TagKey<Item> FALLS_SLOWLY = item("falls_slowly");
    TagKey<Item> PIES = item("pies");
    TagKey<Item> CAN_CUT_PIE = item("can_cut_pie");

    TagKey<Item> MAGIC_FEATHERS = item("magic_feathers");

    TagKey<Item> SHADES = item("shades");
    TagKey<Item> CHANGELING_EDIBLE = item("food_types/changeling_edible");
    TagKey<Item> SPOOKED_MOB_DROPS = item("spooked_mob_drops");
    TagKey<Item> IS_DELIVERED_AGGRESSIVELY = item("is_delivered_aggressively");
    TagKey<Item> FLOATS_ON_CLOUDS = item("floats_on_clouds");

    TagKey<Item> POLEARMS = item("polearms");
    TagKey<Item> APPLE_SEEDS = item("apple_seeds");

    TagKey<Item> ACORNS = item("acorns");
    TagKey<Item> BASKETS = item("baskets");

    TagKey<Block> GLASS_PANES = block("glass_panes");
    TagKey<Block> GLASS_BLOCKS = block("glass_blocks");
    TagKey<Block> FRAGILE = block("fragile");
    TagKey<Block> INTERESTING = block("interesting");
    TagKey<Block> CATAPULT_IMMUNE = block("catapult_immune");

    TagKey<Block> CRYSTAL_HEART_BASE = block("crystal_heart_base");
    TagKey<Block> CRYSTAL_HEART_ORNAMENT = block("crystal_heart_ornament");

    TagKey<Block> POLEARM_MINEABLE = block("mineable/polearm");

    TagKey<EntityType<?>> TRANSFORMABLE_ENTITIES = entity("transformable");

    TagKey<StatusEffect> PINEAPPLE_EFFECTS = effect("pineapple_effects");

    TagKey<DamageType> BREAKS_SUNGLASSES = damage("breaks_sunglasses");
    TagKey<DamageType> SPELLBOOK_IMMUNE_TO = damage("spellbook_immune_to");

    TagKey<DimensionType> HAS_NO_ATMOSPHERE = dimension("has_no_atmosphere");

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

    static TagKey<DamageType> damage(String name) {
        return TagKey.of(RegistryKeys.DAMAGE_TYPE, Unicopia.id(name));
    }

    static TagKey<DimensionType> dimension(String name) {
        return TagKey.of(RegistryKeys.DIMENSION_TYPE, new Identifier("c", name));
    }

    static void bootstrap() {
        Toxics.bootstrap();
    }
}
