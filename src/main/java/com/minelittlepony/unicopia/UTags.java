package com.minelittlepony.unicopia;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.item.Item;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public interface UTags {
    Tag<Item> CURSED_ARTEFACTS = register("cursed_artefacts");
    Tag<Item> HAMMERPACE_IMMUNE = register("hammerspace_immune");
    Tag<Item> APPLES = register("apples");
    Tag<Item> FRESH_TOMATOES = register("fresh_tomatoes");
    Tag<Item> FESH_APPLES = register("fresh_apples");

    Tag<Item> SHARDS = register("shards");
    Tag<Item> SHELLS = register("shells");

    Tag<Item> FIRE_ELEMENTALS = register("fire_elementals");
    Tag<Item> ICE_ELEMENTALS = register("ice_elementals");

    Tag<Item> LIGHT_ELEMENTALS = register("light_elementals");
    Tag<Item> DARK_ELEMENTALS = register("dark_elementals");

    Tag<Item> LIFE_ELEMENTALS = register("life_elementals");
    Tag<Item> ROTTING_ELEMENTALS = register("rotting_elementals");
    Tag<Item> BLOOD_ELEMENTALS = register("death_elementals");

    Tag<Item> UNALIGNED = register("harmonic_elementals");

    Tag<Item> SIGHT_ELEMENTALS = register("sight_elementals");
    Tag<Item> SOUND_ELEMENTALS = register("sound_elementals");
    Tag<Item> MAGIC_ENERGIC = register("knowledge_elementals");

    Tag<Item> APPLE_BLOOM_SPIRIT = register("apple_bloom_spirit");
    Tag<Item> SCOOTALOO_SPIRIT = register("scootaloo_spirit");
    Tag<Item> SWEETIE_BELLE_SPIRIT = register("sweetie_belle_spirit");

    Tag<Item> NON_TOXIC = register("non_toxic");
    Tag<Item> FAIRLY_TOXIC = register("fairly_toxic");
    Tag<Item> SEVERELY_TOXIC = register("severely_toxic");

    static Tag<Item> register(String name) {
        return TagRegistry.item(new Identifier("unicopia", name));
    }

    static void bootstrap() { }
}
