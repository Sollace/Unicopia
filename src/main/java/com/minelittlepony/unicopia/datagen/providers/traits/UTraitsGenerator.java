package com.minelittlepony.unicopia.datagen.providers.traits;

import java.util.function.BiConsumer;

import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.Unicopia;
import com.minelittlepony.unicopia.ability.magic.spell.trait.SpellTraits;
import com.minelittlepony.unicopia.ability.magic.spell.trait.Trait;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader.TraitStream;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader.TraitStream.TraitMap;
import com.minelittlepony.unicopia.ability.magic.spell.trait.TraitLoader.TraitStream.TraitSet;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.server.world.UTreeGen;

import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.util.Identifier;

public class UTraitsGenerator extends TraitsGenerator {

    @Override
    public void generate(WrapperLookup registries, BiConsumer<Identifier, TraitStream> exporter) {
        var sandwichTraits = SpellTraits.builder()
                .with(Trait.LIFE, 2)
                .with(Trait.EARTH, 1);
        var appleTraits = SpellTraits.of(Trait.LIFE, 3);
        var zapAppleTraits = SpellTraits.of(Trait.CHAOS, 5);
        var rottingTraits = SpellTraits.of(Trait.ROT, 2);
        var healthyFoodTraits = SpellTraits.builder()
                .with(Trait.LIFE, 4)
                .with(Trait.GENEROSITY, 10);
        var candyTraits = SpellTraits.builder().with(Trait.EARTH, 7).with(Trait.STRENGTH, 3);
        var electrifiedTraits = SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.POWER, 10);

        exporter.accept(Unicopia.id("chitin"), TraitSet.builder(SpellTraits.builder()
                    .with(Trait.EARTH, 9)
                    .with(Trait.DARKNESS, 9)
                    .with(Trait.KINDNESS, -3)
                )
                .tag(UTags.Items.CHITIN_BLOCKS) // Fixed chitin blocks not having their traits assigned
                .item(UBlocks.SLIME_PUSTULE)
                .item(UItems.CARAPACE)
                .build());
        exporter.accept(Unicopia.id("food"), TraitMap.builder()
                .item(UItems.ZAP_APPLE, zapAppleTraits).item(UItems.COOKED_ZAP_APPLE, zapAppleTraits.multiply(2))
                .item(UItems.ROTTEN_APPLE, appleTraits.add(rottingTraits)) // Added LIFE x3 to rotten_apple
                .item(UItems.DAFFODIL_DAISY_SANDWICH, sandwichTraits)
                .item(UItems.HAY_BURGER, sandwichTraits)
                .item(UItems.HAY_FRIES, sandwichTraits)
                .item(UItems.HORSE_SHOE_FRIES, sandwichTraits.build().add(SpellTraits.of(Trait.STRENGTH, 11)))
                .item(UItems.CRISPY_HAY_FRIES, sandwichTraits.build().add(SpellTraits.of(Trait.HAPPINESS, 2)))
                .item(UItems.WHEAT_WORMS, sandwichTraits.build().multiply(2).add(SpellTraits.of(Trait.EARTH, 4)))
                .item(UItems.CIDER, SpellTraits.builder().with(Trait.CHAOS, 1).with(Trait.DARKNESS, 1))
                .item(UItems.JUICE, SpellTraits.builder().with(Trait.CHAOS, -1).with(Trait.DARKNESS, -9))
                .item(UItems.BURNED_JUICE, SpellTraits.builder().with(Trait.CHAOS, -1).with(Trait.DARKNESS, -19))
                .item(UItems.PINEAPPLE_CROWN, SpellTraits.of(Trait.LIFE, 6))
                .tag(UConventionalTags.Items.BANANAS, SpellTraits.builder().with(Trait.LIFE, 5).with(Trait.GENEROSITY, 3)) // changed to tag
                .tag(UConventionalTags.Items.PINEAPPLES, healthyFoodTraits) // changed to tag
                .tag(UConventionalTags.Items.MANGOES, SpellTraits.builder().with(Trait.EARTH, 2).with(Trait.LIFE, 1)) // changed to tag
                .tag(UConventionalTags.Items.PINECONES, SpellTraits.builder().with(Trait.HAPPINESS, -1).with(Trait.LIFE, 1)) // changed to tag
                .tag(UConventionalTags.Items.GRAIN, SpellTraits.builder().with(Trait.HAPPINESS, 1).with(Trait.LIFE, 1)) // fixed oat seeds having their traits assigned and changed to tag
                .tag(UConventionalTags.Items.OATMEALS, SpellTraits.builder().with(Trait.HAPPINESS, 4))
                .tag(UTags.Items.BAKED_GOODS, SpellTraits.builder().with(Trait.HAPPINESS, 2)) // changed to tag
                .item(UItems.APPLE_PIE_SLICE, healthyFoodTraits)
                .tag(ConventionalItemTags.CANDY_FOODS, candyTraits) // Fixed candy not having their traits assigned
                .item(UBlocks.ZAP_BULB, electrifiedTraits.build().add(SpellTraits.of(Trait.LIFE, 5)))
                .item(UItems.MUFFIN, SpellTraits.of(Trait.HAPPINESS, 11))
                .tag(UConventionalTags.Items.ACORNS, SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.EARTH, 4).with(Trait.STRENGTH, 3)) // changed to tag
                .build());
        exporter.accept(Unicopia.id("intellectual_objects_ordered"), TraitSet.builder(SpellTraits.builder().with(Trait.KNOWLEDGE, 9).with(Trait.ORDER, 9))
                .item(UBlocks.WEATHER_VANE)
                .item(UItems.GIANT_BALLOON)
                .build());
        exporter.accept(Unicopia.id("from_the_ground"), TraitMap.builder()
                .item(UItems.CRYSTAL_HEART, SpellTraits.builder().with(Trait.POWER, 17).with(Trait.ORDER, 19))
                .item(UItems.CRYSTAL_SHARD, SpellTraits.builder().with(Trait.POWER, 6).with(Trait.ORDER, 2))
                .item(UItems.GEMSTONE, SpellTraits.builder().with(Trait.POWER, -1).with(Trait.ORDER, 1))
                .item(UItems.PEBBLES, SpellTraits.of(Trait.EARTH, 3))
                .item(UItems.ROCK, SpellTraits.of(Trait.EARTH, 6))
                .item(UItems.TOM, SpellTraits.of(Trait.EARTH, 11))
                .item(UItems.WEIRD_ROCK, SpellTraits.builder().with(Trait.EARTH, 16).with(Trait.CHAOS, 9))
                .item(UItems.ROCK_STEW, SpellTraits.builder().with(Trait.EARTH, 9).with(Trait.CHAOS, 16))
                .item(UItems.TOAST, SpellTraits.of(Trait.EARTH, 3))
                .item(UItems.BURNED_TOAST, SpellTraits.builder().with(Trait.EARTH, 3).with(Trait.CHAOS, 1))
                .item(UItems.JAM_TOAST, SpellTraits.builder().with(Trait.STRENGTH, 1).with(Trait.POWER, 11).with(Trait.EARTH, 3))
                .build());
        exporter.accept(Unicopia.id("special"), TraitMap.builder()
                .item(UItems.EMPTY_JAR, SpellTraits.of(Trait.AIR, 9))
                .item(UItems.RAIN_CLOUD_JAR, SpellTraits.builder().with(Trait.WATER, 6).with(Trait.AIR, 8))
                .item(UItems.STORM_CLOUD_JAR, SpellTraits.builder().with(Trait.WATER, 6).with(Trait.AIR, 8))
                .item(UBlocks.LIGHTNING_JAR, SpellTraits.builder().with(Trait.POWER, 9).with(Trait.WATER, 6).with(Trait.AIR, 8))
                .item(UItems.ZAP_APPLE_JAM_JAR, SpellTraits.builder().with(Trait.AIR, 8).with(Trait.CHAOS, 6))
                .item(UItems.MUG, SpellTraits.builder().with(Trait.EARTH, 1).with(Trait.ORDER, 1))
                .item(UItems.GOLDEN_FEATHER, SpellTraits.builder().with(Trait.ORDER, 16).with(Trait.CHAOS, -14))
                .item(UItems.BUTTERFLY, SpellTraits.builder().with(Trait.DARKNESS, 4).with(Trait.BLOOD, 7))
                .item(UItems.SPELLBOOK, SpellTraits.builder().with(Trait.POWER, 18).with(Trait.DARKNESS, 7))
                .item(UItems.PEGASUS_AMULET, SpellTraits.builder().with(Trait.POWER, 27).with(Trait.ORDER, 10))
                .item(UItems.ALICORN_AMULET, SpellTraits.builder().with(Trait.STRENGTH, 23).with(Trait.POWER, 11).with(Trait.ORDER, -10).with(Trait.DARKNESS, 22))
                .tag(UTags.Items.COOLS_OFF_KIRINS, SpellTraits.of(Trait.WATER, 6))
                .item(UBlocks.HIVE, SpellTraits.builder().with(Trait.LIFE, 10).with(Trait.EARTH, 9).with(Trait.KINDNESS, -3).with(Trait.DARKNESS, 8))
                .tag(UTags.Items.TINTED_SHADES, SpellTraits.of(Trait.DARKNESS, 3))
                .item(UItems.BROKEN_SUNGLASSES, SpellTraits.builder().with(Trait.CHAOS, 3).with(Trait.DARKNESS, 3))
                .item(UItems.SALT_CUBE, SpellTraits.of(Trait.POWER, 3))
                .build());
        exporter.accept(Unicopia.id("from_the_sky_with_kindness"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.AIR, 9).with(Trait.KINDNESS, 10))
                .tag(UTags.Items.MAGIC_FEATHERS)
                .build());
        exporter.accept(Unicopia.id("organic_plant_derived_artificial"), TraitSet.builder(boatTraits())
                .tag(UTags.Items.BASKETS)
                .build());
        exporter.accept(Unicopia.id("magical"), TraitSet.builder(SpellTraits.builder().with(Trait.KINDNESS, 10).with(Trait.HAPPINESS, 10))
                .tag(UTags.Items.GROUP_UNICORN)
                .item(UItems.CURING_JOKE)
                .build());
        exporter.accept(Unicopia.id("uplifting_trinkets"), TraitMap.builder()
                .item(UItems.MUSIC_DISC_CRUSADE, SpellTraits.builder().with(Trait.ORDER, 10).with(Trait.HAPPINESS, 9))
                .item(UItems.MUSIC_DISC_PET, SpellTraits.builder().with(Trait.FOCUS, 9).with(Trait.ORDER, 10).with(Trait.KINDNESS, 8))
                .item(UItems.MUSIC_DISC_POPULAR, SpellTraits.builder().with(Trait.FOCUS, 9).with(Trait.ORDER, 10).with(Trait.GENEROSITY, 8))
                .item(UItems.MUSIC_DISC_FUNK, SpellTraits.of(Trait.CHAOS, -10))
                .item(UItems.FRIENDSHIP_BRACELET, SpellTraits.builder().with(Trait.ORDER, 2).with(Trait.HAPPINESS, 1).with(Trait.GENEROSITY, 1))
                .item(UItems.PEARL_NECKLACE, SpellTraits.builder().with(Trait.KNOWLEDGE, 2).with(Trait.CHAOS, 7))
                .item(UItems.LIGHTNING_JAR, SpellTraits.builder().with(Trait.KNOWLEDGE, 2).with(Trait.POWER, 8).with(Trait.CHAOS, 1))
                .build());
        exporter.accept(Unicopia.id("organic_living"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 10))
                .item(UBlocks.MYSTERIOUS_EGG)
                .item(UItems.TOM)
                .build());
        exporter.accept(Unicopia.id("from_the_sky"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.AIR, 9).with(Trait.KINDNESS, -6))
                .item(UItems.GRYPHON_FEATHER)
                .build());
        exporter.accept(Unicopia.id("shells"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.EARTH, 1).with(Trait.WATER, 7))
                .tag(UTags.Items.SPECIAL_SHELLS)
                .tag(UTags.Items.SHELLS)
                .build());
        exporter.accept(Unicopia.id("soft_and_kind"), TraitSet.builder(SpellTraits.of(Trait.KINDNESS, 3))
                .tag(UTags.Items.BED_SHEETS)
                .tag(UTags.Items.PIES)
                .tag(UTags.Items.FLOATS_ON_CLOUDS)
                .build());
        exporter.accept(Unicopia.id("love"), TraitSet.builder(SpellTraits.builder().with(Trait.KINDNESS, 10).with(Trait.HAPPINESS, 10))
                .tag(UTags.Items.CONTAINER_WITH_LOVE)
                .tag(UTags.Items.CLOUD_BLOCKS)
                .build());
        exporter.accept(Unicopia.id("gilded"), TraitSet.builder(SpellTraits.builder().with(Trait.STRENGTH, 10).with(Trait.GENEROSITY, -3))
                .item(UItems.GOLDEN_WING)
                .item(UItems.GOLDEN_OAK_SEEDS)
                .item(UBlocks.GOLDEN_OAK_LEAVES)
                .item(UBlocks.GOLDEN_OAK_LOG)
                .item(UTreeGen.GOLDEN_OAK_TREE.sapling().orElseThrow())
                .build());
        exporter.accept(Unicopia.id("organic_plant_derived_zap"), TraitSet.builder(SpellTraits.builder().with(Trait.LIFE, 1).with(Trait.EARTH, 2).with(Trait.CHAOS, 3))
                .item(UBlocks.ZAP_WOOD)
                .item(UBlocks.ZAP_LOG)
                .item(UBlocks.STRIPPED_ZAP_WOOD)
                .item(UBlocks.STRIPPED_ZAP_LOG)
                .item(UBlocks.ZAP_LEAVES)
                .item(UBlocks.FLOWERING_ZAP_LEAVES)
                .build());

    }

}
