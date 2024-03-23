package com.minelittlepony.unicopia.datagen.providers;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.advancement.CustomEventCriterion;
import com.minelittlepony.unicopia.advancement.RaceChangeCriterion;
import com.minelittlepony.unicopia.advancement.RacePredicate;
import com.minelittlepony.unicopia.advancement.SendViaDragonBreathScrollCriterion;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.item.enchantment.UEnchantments;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.AdvancementRewards;
import net.minecraft.advancement.CriterionMerger;
import net.minecraft.advancement.criterion.ConsumeItemCriterion;
import net.minecraft.advancement.criterion.CriterionConditions;
import net.minecraft.advancement.criterion.EnchantedItemCriterion;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.advancement.criterion.OnKilledCriterion;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.TagPredicate;
import net.minecraft.predicate.entity.DamageSourcePredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.predicate.entity.LootContextPredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class UAdvancementsProvider extends FabricAdvancementProvider {
    public UAdvancementsProvider(FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateAdvancement(Consumer<Advancement> consumer) {
        AdvancementDisplayBuilder.create(UItems.ALICORN_BADGE).criterion("crafting_table", hasItems(Items.CRAFTING_TABLE)).build(consumer, "root").children(root -> {
            createTribeRootAdvancement(consumer, root, Race.EARTH).children(consumer, this::generateEarthTribeAdvancementsTree);
            createTribeRootAdvancement(consumer, root, Race.BAT).children(consumer, this::generateBatTribeAdvancementsTree);
            createTribeRootAdvancement(consumer, root, Race.PEGASUS).children(consumer, this::generatePegasusTribeAdvancementsTree);
            createTribeRootAdvancement(consumer, root, Race.UNICORN).children(consumer, this::generateUnicornTribeAdvancementsTree);

            root.child(UItems.DRAGON_BREATH_SCROLL).criterion("has_scroll", hasItems(UItems.DRAGON_BREATH_SCROLL)).build(consumer, "take_a_note").children(p -> {
                p.child(UItems.DRAGON_BREATH_SCROLL).criterion("send_scroll", dragonScroll(false, Items.WRITTEN_BOOK)).build(consumer, "dear_princess");
                p.child(UItems.IMPORTED_OATS).hidden().frame(AdvancementFrame.CHALLENGE)
                    .criterion("send_oats", dragonScroll(false, UItems.OATS, UItems.IMPORTED_OATS))
                    .criterion("receieve_oats", dragonScroll(true, UItems.OATS, UItems.IMPORTED_OATS))
                    .criteriaMerger(CriterionMerger.OR)
                    .build(consumer, "imported_oats");
                p.child(Items.CHIPPED_ANVIL).hidden().frame(AdvancementFrame.CHALLENGE).criterion("ding_sun", dingCelestia(Set.of(), Set.of(Race.BAT))).build(consumer, "blasphemy");
                p.child(Items.CHIPPED_ANVIL).hidden().frame(AdvancementFrame.CHALLENGE).criterion("ding_sun", dingCelestia(Set.of(Race.BAT), Set.of())).build(consumer, "sweet_sweet_revenge");
            });
            root.child(UItems.OATS).criterion("has_oats", hasItems(UItems.OATS)).build(consumer, "what_the_hay");
            root.child(UItems.IRON_HORSE_SHOE).criterion("killed_entity_with_horseshoe", killWithItems(UTags.FROM_HORSESHOES)).build(consumer, "dead_ringer");
            root.child(UItems.PINECONE).frame(AdvancementFrame.CHALLENGE).criterion("eat_pinecone", ConsumeItemCriterion.Conditions.item(UItems.PINECONE)).build(consumer, "eat_pinecone");
            root.child(UItems.GIANT_BALLOON).criterion("ride_balloon", CustomEventCriterion.create("ride_balloon")).build(consumer, "travelling_in_style");
            root.child(UItems.MUFFIN).hidden().criterion("has_muffin", hasItems(UItems.MUFFIN)).build(consumer, "baked_bads");
            root.child(UItems.HORSE_SHOE_FRIES).criterion("has_horse_shoe_fries", hasItems(UItems.HORSE_SHOE_FRIES)).build(consumer, "lucky");
            root.child(UItems.TOAST).criterion("has_toast", hasItems(UItems.TOAST)).build(consumer, "toast")
                .child(UItems.BURNED_TOAST).hidden().criterion("has_burned_toast", hasItems(UItems.BURNED_TOAST)).build(consumer, "burn_toast");
            root.child(UItems.GREEN_APPLE).criterion("has_apple", hasItems(UTags.FRESH_APPLES)).build(consumer, "apple_route").children(p -> {
                p.child(UItems.SWEET_APPLE)
                    .criterion("has_all_apples", hasItems(Items.APPLE, UItems.GREEN_APPLE, UItems.SWEET_APPLE, UItems.SOUR_APPLE, UItems.ROTTEN_APPLE, UItems.ZAP_APPLE, UItems.COOKED_ZAP_APPLE, Items.GOLDEN_APPLE))
                    .build(consumer, "sweet_apple_acres");
                p.child(UItems.ZAP_BULB).criterion("has_zap_apple", hasItems(UItems.ZAP_APPLE)).build(consumer, "trick_apple").children(pp -> {
                  pp.child(UItems.ZAP_APPLE).hidden().criterion("eat_trick_apple", CustomEventCriterion.createFlying("eat_trick_apple")).build(consumer, "eat_trick_apple");
                  pp.child(UItems.ZAP_APPLE).hidden().criterion("feed_trick_apple", CustomEventCriterion.createFlying("feed_trick_apple")).build(consumer, "feed_trick_apple");
                });
                p.child(UItems.JUICE).criterion("has_juice", hasItems(UItems.JUICE)).build(consumer, "juice")
                 .child(UItems.BURNED_JUICE).hidden().criterion("has_burned_juice", hasItems(UItems.BURNED_JUICE)).build(consumer, "burn_juice")
                 .child(UItems.CIDER).visible().criterion("has_cider", hasItems(UItems.CIDER)).rewards(AdvancementRewards.Builder.experience(12)).build(consumer, "brew_cider");
            });
        });

        generateEnchantmentsAdvancementsTree(consumer);
    }

    private AdvancementDisplayBuilder.Parent createTribeRootAdvancement(Consumer<Advancement> consumer, AdvancementDisplayBuilder.Parent root, Race race) {
        AdvancementDisplayBuilder builder = root.child(Registries.ITEM.get(race.getId().withSuffixedPath("_badge"))).showToast().announce().group(race.getId().getPath())
                .criterion("be_" + race.getId().getPath(), new RaceChangeCriterion.Conditions(LootContextPredicate.EMPTY, race));

        if (race == Race.UNICORN) {
            builder
                .criterion("be_alicorn", new RaceChangeCriterion.Conditions(LootContextPredicate.EMPTY, Race.ALICORN))
                .criteriaMerger(CriterionMerger.OR);
        }

        return builder.build(consumer, race.getId().getPath() + "_route");
    }

    private void generateEarthTribeAdvancementsTree(Consumer<Advancement> consumer, AdvancementDisplayBuilder.Parent parent) {
        parent.child(UItems.ROCK).criterion("has_rock", hasItems(UItems.ROCK)).build(consumer, "born_on_a_rock_farm").children(p -> {
            p.child(UItems.PEBBLES).criterion("killed_entity_with_rock", killWithItems(UTags.FROM_ROCKS)).build(consumer, "sticks_and_stones");
            p.child(UItems.WEIRD_ROCK).hidden().criterion("has_rock", hasItems(UItems.WEIRD_ROCK)).build(consumer, "thats_unusual");
        });
    }

    private void generatePegasusTribeAdvancementsTree(Consumer<Advancement> consumer, AdvancementDisplayBuilder.Parent parent) {
        parent.child(Items.PHANTOM_MEMBRANE).hidden().frame(AdvancementFrame.CHALLENGE).criterion("deter_phantom", CustomEventCriterion.createFlying("kill_phantom_while_flying")).rewards(AdvancementRewards.Builder.experience(100)).build(consumer, "deter_phantom");
        parent.child(Items.GLASS_PANE).criterion("break_window", CustomEventCriterion.createFlying("break_window")).rewards(AdvancementRewards.Builder.experience(10)).build(consumer, "rainbow_crash");
        parent.child(UItems.PEGASUS_BADGE).criterion("fly_through_the_pain", CustomEventCriterion.createFlying("second_wind")).rewards(AdvancementRewards.Builder.experience(10)).build(consumer, "second_wind");
        parent.child(UItems.EMPTY_JAR).criterion("has_empty_jar", hasItems(UItems.EMPTY_JAR)).build(consumer, "jar")
            .child(UItems.RAIN_CLOUD_JAR).criterion("has_cloud_jar", hasItems(UTags.CLOUD_JARS)).rewards(AdvancementRewards.Builder.experience(55)).build(consumer, "gotcha");
        parent.child(UItems.LIGHTNING_JAR).frame(AdvancementFrame.CHALLENGE).criterion("lightning_strike", CustomEventCriterion.createFlying("lightning_strike")).rewards(AdvancementRewards.Builder.experience(30)).build(consumer, "mid_flight_interruption").children(p -> {
            p.child(UItems.LIGHTNING_JAR).hidden().frame(AdvancementFrame.CHALLENGE).apply(d -> applyLightningBugCriterions(d, RacePredicate.of(Set.of(Race.CHANGELING), Set.of()), 10, 90)).build(consumer, "lightning_bug");
            p.child(UItems.LIGHTNING_JAR).hidden().frame(AdvancementFrame.CHALLENGE).apply(d -> applyLightningBugCriterions(d, RacePredicate.of(Set.of(), Set.of(Race.CHANGELING)), 10, 90)).build(consumer, "wonder_bolt");
        });
        parent.child(UItems.PEGASUS_FEATHER).hidden().frame(AdvancementFrame.CHALLENGE).criterion("shed_feather", CustomEventCriterion.createFlying("shed_feather")).rewards(AdvancementRewards.Builder.experience(1)).build(consumer, "molting_season_1")
            .child(UItems.PEGASUS_FEATHER).apply(d -> applyShedFeatherCriterions(d, 2, 2)).build(consumer, "molting_season_2")
            .child(UItems.PEGASUS_FEATHER).apply(d -> applyShedFeatherCriterions(d, 4, 8)).build(consumer, "molting_season_3")
            .child(UItems.PEGASUS_FEATHER).apply(d -> applyShedFeatherCriterions(d, 8, 20)).build(consumer, "molting_season_4")
            .child(UItems.PEGASUS_FEATHER).apply(d -> applyShedFeatherCriterions(d, 16, 40)).build(consumer, "molting_season_5")
            .child(UItems.PEGASUS_FEATHER).apply(d -> applyShedFeatherCriterions(d, 32, 80)).build(consumer, "molting_season_6")
            .child(UItems.PEGASUS_FEATHER).apply(d -> applyShedFeatherCriterions(d, 64, 200)).build(consumer, "molting_season_7")
            .child(UItems.PEGASUS_FEATHER).apply(d -> applyShedFeatherCriterions(d, 128, 500)).build(consumer, "molting_season_8")
            .child(UItems.PEGASUS_FEATHER).apply(d -> applyShedFeatherCriterions(d, 256, 1000)).build(consumer, "molting_season_9")
            .child(UItems.PEGASUS_FEATHER).apply(d -> applyShedFeatherCriterions(d, 512, 2280)).build(consumer, "molting_season_10")
            .child(UItems.GOLDEN_FEATHER).apply(d -> applyShedFeatherCriterions(d, 1024, 4560)).build(consumer, "molting_season_11")
            .child(UItems.GOLDEN_FEATHER).apply(d -> applyShedFeatherCriterions(d, 2048, 10000)).frame(AdvancementFrame.GOAL).build(consumer, "dedicated_flier");
    }

    private AdvancementDisplayBuilder applyShedFeatherCriterions(AdvancementDisplayBuilder builder, int repeats, int experience) {
        for (int i = 1; i <= repeats; i++) {
            builder.criterion("shed_feather_" + i, CustomEventCriterion.createFlying("shed_feather", i));
        }
        return builder.criteriaMerger(CriterionMerger.AND).rewards(AdvancementRewards.Builder.experience(experience));
    }

    private AdvancementDisplayBuilder applyLightningBugCriterions(AdvancementDisplayBuilder builder, RacePredicate race, int repeats, int experience) {
        for (int i = 1; i <= repeats; i++) {
            builder.criterion("lightning_struck_player_" + i, new CustomEventCriterion.Conditions(LootContextPredicate.EMPTY, "lightning_struck_player", race, true, i));
        }
        return builder.criteriaMerger(CriterionMerger.AND).rewards(AdvancementRewards.Builder.experience(experience));
    }

    private void generateUnicornTribeAdvancementsTree(Consumer<Advancement> consumer, AdvancementDisplayBuilder.Parent parent) {
        parent.child(UItems.SPELLBOOK).criterion("has_spellbook", hasItems(UItems.SPELLBOOK)).build(consumer, "books").children(p -> {
            p.child(UItems.CRYSTAL_SHARD).criterion("has_shard", hasItems(UItems.CRYSTAL_SHARD)).build(consumer, "crystaline").children(pp -> {
               pp.child(UItems.CRYSTAL_HEART).criterion("power_up_heart", CustomEventCriterion.create("power_up_heart")).rewards(AdvancementRewards.Builder.experience(105)).build(consumer, "power_up_heart");
            });
            p.child(UItems.ALICORN_AMULET).criterion("has_alicorn_amulet", hasItems(UItems.ALICORN_AMULET)).build(consumer, "tempting")
             .child(Items.CRYING_OBSIDIAN).criterion("light_altar", CustomEventCriterion.create("light_altar")).build(consumer, "hello_darkness_my_old_friend")
             .child(UItems.BROKEN_ALICORN_AMULET).frame(AdvancementFrame.GOAL).criterion("defeat_sombra", CustomEventCriterion.create("defeat_sombra")).rewards(AdvancementRewards.Builder.experience(2000)).build(consumer, "save_the_day")
             .children(pp -> {
                 pp.child(UItems.UNICORN_AMULET).frame(AdvancementFrame.GOAL).criterion("obtain_the_thing", hasItems(UItems.UNICORN_AMULET)).rewards(AdvancementRewards.Builder.experience(1100)).build(consumer, "ascension");
                 pp.child(UItems.BROKEN_ALICORN_AMULET).hidden().frame(AdvancementFrame.CHALLENGE).criterion("defeat_sombra_again", CustomEventCriterion.create("defeat_sombra", 2)).rewards(AdvancementRewards.Builder.experience(2000)).build(consumer, "doctor_sombrero");
             });
            p.child(Items.WATER_BUCKET).criterion("split_sea", CustomEventCriterion.create("split_sea")).rewards(AdvancementRewards.Builder.experience(105)).build(consumer, "split_the_sea");
        });

        parent.child(UItems.PEGASUS_AMULET).hidden().frame(AdvancementFrame.CHALLENGE).criterion("teleport_above_world", CustomEventCriterion.create("teleport_above_world")).rewards(AdvancementRewards.Builder.experience(100)).build(consumer, "a_falling_wizard");

    }

    private void generateBatTribeAdvancementsTree(Consumer<Advancement> consumer, AdvancementDisplayBuilder.Parent parent) {
        parent.child(Items.LIGHT).criterion("look_into_sun", CustomEventCriterion.create("look_into_sun")).build(consumer, "praise_the_sun").children(p -> {
            p.child(UItems.SUNGLASSES).criterion("wear_shades", CustomEventCriterion.create("wear_shades")).build(consumer, "cool_potato");
            p.child(Items.BLACK_CANDLE).frame(AdvancementFrame.CHALLENGE).criterion("screech_twenty_mobs", CustomEventCriterion.createFlying("screech_twenty_mobs")).build(consumer, "screech_twenty_mobs").children(pp -> {
                pp.child(Items.BRICK).frame(AdvancementFrame.CHALLENGE).criterion("super_scare_entity", CustomEventCriterion.createFlying("super_scare_entity")).build(consumer, "extra_spooky");
            });
            p.child(Items.BLACK_CANDLE).frame(AdvancementFrame.CHALLENGE).criterion("screech_self", CustomEventCriterion.createFlying("screech_self")).build(consumer, "screech_self");
        });
    }

    private void generateEnchantmentsAdvancementsTree(Consumer<Advancement> consumer) {
        AdvancementDisplayBuilder.create(Items.NETHERITE_SCRAP)
            .criterion("enchant_with_consumption", enchant(UEnchantments.CONSUMPTION))
            .rewards(AdvancementRewards.Builder.experience(120))
            .parent(new Identifier("story/enchant_item"))
            .group("enchanting")
            .build(consumer, "experimental")
            .child(Items.NETHERITE_PICKAXE)
                .criterion("use_consumption", CustomEventCriterion.create("use_consumption"))
                .rewards(AdvancementRewards.Builder.experience(1200))
                .group("enchanting")
                .hidden()
                .build(consumer, "xp_miner");
        AdvancementDisplayBuilder.create(Items.GOLDEN_APPLE)
            .criterion("enchant_with_heart_bound", enchant(UEnchantments.HEART_BOUND))
            .rewards(AdvancementRewards.Builder.experience(120))
            .group("enchanting")
            .build(consumer, "hearts_stronger_than_horses")
            .child(Items.GOLDEN_PICKAXE)
                .criterion("use_soulmate", CustomEventCriterion.create("use_soulmate"))
                .rewards(AdvancementRewards.Builder.experience(1200))
                .group("enchanting")
                .hidden()
                .build(consumer, "soulmate");
    }

    public static CriterionConditions enchant(Enchantment enchantment) {
        return new EnchantedItemCriterion.Conditions(LootContextPredicate.EMPTY, ItemPredicate.Builder.create()
                .enchantment(new EnchantmentPredicate(enchantment, NumberRange.IntRange.ANY))
                .build(), NumberRange.IntRange.ANY);
    }

    public static CriterionConditions dragonScroll(boolean receiving, ItemConvertible...items) {
        return dragonScroll(receiving, ItemPredicate.Builder.create().items(items).build());
    }

    public static CriterionConditions dragonScroll(boolean receiving, ItemPredicate items) {
        return new SendViaDragonBreathScrollCriterion.Conditions(
                LootContextPredicate.EMPTY,
                ItemPredicate.ANY,
                receiving,
                Optional.empty(),
                TriState.DEFAULT,
                Optional.empty(),
                RacePredicate.EMPTY
        );
    }

    static CriterionConditions hasItems(ItemConvertible...items) {
        return InventoryChangedCriterion.Conditions.items(items);
    }

    static CriterionConditions hasItems(TagKey<Item> items) {
        return InventoryChangedCriterion.Conditions.items(ItemPredicate.Builder.create().tag(items).build());
    }

    static CriterionConditions killWithItems(TagKey<DamageType> tag) {
        return OnKilledCriterion.Conditions.createPlayerKilledEntity(EntityPredicate.ANY,
                    DamageSourcePredicate.Builder.create().tag(TagPredicate.expected(tag)).build()
        );
    }

    static CriterionConditions dingCelestia(Set<Race> includeTribes, Set<Race> excludeTribes) {
        return new SendViaDragonBreathScrollCriterion.Conditions(
                LootContextPredicate.EMPTY,
                ItemPredicate.Builder.create().tag(UTags.IS_DELIVERED_AGGRESSIVELY).build(), false,
                Optional.of("princess celestia"),
                TriState.FALSE,
                Optional.of("dings_on_celestias_head"),
                RacePredicate.of(includeTribes, excludeTribes));
    }
}
