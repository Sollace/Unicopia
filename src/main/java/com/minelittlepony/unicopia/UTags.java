package com.minelittlepony.unicopia;

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
    interface Items {
        TagKey<Item> ZAP_LOGS = item("zap_logs");
        TagKey<Item> WAXED_ZAP_LOGS = item("waxed_zap_logs");
        TagKey<Item> PALM_LOGS = item("palm_logs");
        TagKey<Item> CLOUD_BEDS = item("cloud_beds");
        TagKey<Item> CLOUD_SLABS = item("cloud_slabs");
        TagKey<Item> CLOUD_STAIRS = item("cloud_stairs");
        TagKey<Item> CLOUD_BLOCKS = item("cloud_blocks");
        TagKey<Item> CHITIN_BLOCKS = item("chitin_blocks");

        TagKey<Item> FRESH_APPLES = item("fresh_apples");

        TagKey<Item> FALLS_SLOWLY = item("falls_slowly");
        TagKey<Item> PIES = item("pies");
        TagKey<Item> CAN_CUT_PIE = item("can_cut_pie");

        TagKey<Item> MAGIC_FEATHERS = item("magic_feathers");

        TagKey<Item> SHADES = item("shades");
        TagKey<Item> CHANGELING_EDIBLE = item("food_types/changeling_edible");
        TagKey<Item> SPOOKED_MOB_DROPS = item("spooked_mob_drops");
        TagKey<Item> HAS_NO_TRAITS = item("has_no_traits");
        TagKey<Item> IS_DELIVERED_AGGRESSIVELY = item("is_delivered_aggressively");
        TagKey<Item> CONTAINER_WITH_LOVE = item("container_with_love");
        TagKey<Item> FLOATS_ON_CLOUDS = item("floats_on_clouds");
        TagKey<Item> COOLS_OFF_KIRINS = item("cools_off_kirins");
        TagKey<Item> LOOT_BUG_COMMON_DROPS = item("loot_bug_common_drops");
        TagKey<Item> LOOT_BUG_RARE_DROPS = item("loot_bug_rare_drops");
        TagKey<Item> LOOT_BUG_EPIC_DROPS = item("loot_bug_epic_drops");

        TagKey<Item> SHELLS = item("shells");
        TagKey<Item> SPECIAL_SHELLS = item("special_shells");
        TagKey<Item> ROCK_STEWS = item("rock_stews");
        TagKey<Item> BAKED_GOODS = item("baked_goods");

        TagKey<Item> POLEARMS = item("polearms");
        TagKey<Item> HORSE_SHOES = item("horse_shoes");
        TagKey<Item> APPLE_SEEDS = item("apple_seeds");

        TagKey<Item> BASKETS = item("baskets");
        TagKey<Item> BADGES = item("badges");
        TagKey<Item> WOOL_BED_SHEETS = item("wool_bed_sheets");
        TagKey<Item> BED_SHEETS = item("bed_sheets");
        TagKey<Item> CLOUD_JARS = item("cloud_jars");

        TagKey<Item> GROUP_FORAGING = item("groups/foraging");
        TagKey<Item> GROUP_EARTH_PONY = item("groups/earth_pony");
        TagKey<Item> GROUP_UNICORN = item("groups/unicorn");
        TagKey<Item> GROUP_PEGASUS = item("groups/pegasus");
        TagKey<Item> GROUP_BAT_PONY = item("groups/bat_pony");
        TagKey<Item> GROUP_SEA_PONY = item("groups/sea_pony");
        TagKey<Item> GROUP_CHANGELING = item("groups/changeling");

        private static TagKey<Item> item(String name) {
            return TagKey.of(RegistryKeys.ITEM, Unicopia.id(name));
        }
    }

    interface Blocks {
        TagKey<Block> ZAP_LOGS = block("zap_logs");
        TagKey<Block> WAXED_ZAP_LOGS = block("waxed_zap_logs");
        TagKey<Block> PALM_LOGS = block("palm_logs");
        TagKey<Block> CLOUD_BEDS = block("cloud_beds");
        TagKey<Block> CLOUD_SLABS = block("cloud_slabs");
        TagKey<Block> CLOUD_STAIRS = block("cloud_stairs");
        TagKey<Block> CLOUD_BLOCKS = block("cloud_blocks");
        TagKey<Block> CHITIN_BLOCKS = block("chitin_blocks");

        TagKey<Block> FRAGILE = block("fragile");
        TagKey<Block> INTERESTING = block("interesting");
        TagKey<Block> CATAPULT_IMMUNE = block("catapult_immune");
        TagKey<Block> JARS = block("jars");

        TagKey<Block> CRYSTAL_HEART_BASE = block("crystal_heart_base");
        TagKey<Block> CRYSTAL_HEART_ORNAMENT = block("crystal_heart_ornament");
        TagKey<Block> UNAFFECTED_BY_GROW_ABILITY = block("unaffected_by_grow_ability");
        TagKey<Block> KICKS_UP_DUST = block("kicks_up_dust");

        TagKey<Block> POLEARM_MINEABLE = block("mineable/polearm");

        TagKey<Block> BUTTERFLIES_SPAWNABLE_ON = block("butterflies_spawn_on");

        private static TagKey<Block> block(String name) {
            return TagKey.of(RegistryKeys.BLOCK, Unicopia.id(name));
        }
    }

    interface Entities {
        TagKey<EntityType<?>> TRANSFORMABLE = entity("transformable");

        private static TagKey<EntityType<?>> entity(String name) {
            return TagKey.of(RegistryKeys.ENTITY_TYPE, Unicopia.id(name));
        }
    }

    interface DamageTypes {
        TagKey<DamageType> BREAKS_SUNGLASSES = damage("breaks_sunglasses");
        TagKey<DamageType> SPELLBOOK_IMMUNE_TO = damage("spellbook_immune_to");
        TagKey<DamageType> FROM_ROCKS = damage("from_rocks");
        TagKey<DamageType> FROM_HORSESHOES = damage("from_horseshoes");

        private static TagKey<DamageType> damage(String name) {
            return TagKey.of(RegistryKeys.DAMAGE_TYPE, Unicopia.id(name));
        }
    }

    interface DimensionTypes {
        TagKey<DimensionType> HAS_NO_ATMOSPHERE = dimension("has_no_atmosphere");

        private static TagKey<DimensionType> dimension(String name) {
            return TagKey.of(RegistryKeys.DIMENSION_TYPE, new Identifier("c", name));
        }
    }

    interface StatusEffects {
        TagKey<StatusEffect> PINEAPPLE_EFFECTS = effect("pineapple_effects");

        private static TagKey<StatusEffect> effect(String name) {
            return TagKey.of(RegistryKeys.STATUS_EFFECT, Unicopia.id(name));
        }
    }
}
