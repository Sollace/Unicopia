package com.minelittlepony.unicopia.datagen.providers.tag;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import com.minelittlepony.unicopia.Race;
import com.minelittlepony.unicopia.UConventionalTags;
import com.minelittlepony.unicopia.UTags;
import com.minelittlepony.unicopia.block.UBlocks;
import com.minelittlepony.unicopia.datagen.Datagen;
import com.minelittlepony.unicopia.datagen.ItemFamilies;
import com.minelittlepony.unicopia.datagen.UBlockFamilies;
import com.minelittlepony.unicopia.item.BedsheetsItem;
import com.minelittlepony.unicopia.item.UItems;
import com.minelittlepony.unicopia.server.world.UTreeGen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.minecraft.block.Block;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.RegistryWrapper.WrapperLookup;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class UItemTagProvider extends FabricTagProvider.ItemTagProvider {


    private final UBlockTagProvider blockTagProvider;

    public UItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture, UBlockTagProvider blockTagProvider) {
        super(output, registriesFuture, blockTagProvider);
        this.blockTagProvider = blockTagProvider;
    }

    @Override
    public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
        TagBuilder blockTagBuilder = Objects.requireNonNull(blockTagProvider, "Pass Block tag provider via constructor to use copy").getTagBuilder(blockTag);
        TagBuilder itemTagBuilder = getTagBuilder(itemTag);
        blockTagBuilder.build().forEach(entry -> {
            if (entry.canAdd(Registries.ITEM::containsId, tagId -> getTagBuilder(TagKey.of(RegistryKeys.ITEM, tagId)) != null)) {
                itemTagBuilder.add(entry);
            } else {
                Datagen.LOGGER.warn("Cannot copy missing entry {} to item tag {}", entry, itemTag.id());
            }
        });
    }

    @Override
    protected void configure(WrapperLookup arg) {
        copyBlockTags();
        exportConventionalTags();
        getOrCreateTagBuilder(ItemTags.BOOKSHELF_BOOKS).add(UItems.SPELLBOOK);
        getOrCreateTagBuilder(ItemTags.BEDS).add(UItems.CLOTH_BED, UItems.CLOUD_BED);

        getOrCreateTagBuilder(ItemTags.CHEST_BOATS).add(UItems.PALM_CHEST_BOAT);
        getOrCreateTagBuilder(ItemTags.BOATS).add(UItems.PALM_BOAT);
        getOrCreateTagBuilder(ItemTags.CREEPER_DROP_MUSIC_DISCS).add(UItems.MUSIC_DISC_CRUSADE, UItems.MUSIC_DISC_FUNK, UItems.MUSIC_DISC_PET, UItems.MUSIC_DISC_POPULAR);

        getOrCreateTagBuilder(ItemTags.SIGNS).add(UBlocks.PALM_SIGN.asItem());
        getOrCreateTagBuilder(ItemTags.HANGING_SIGNS).add(UBlocks.PALM_HANGING_SIGN.asItem());

        getOrCreateTagBuilder(UTags.Items.HORSE_SHOES).add(ItemFamilies.HORSE_SHOES);
        getOrCreateTagBuilder(UTags.Items.POLEARMS).add(ItemFamilies.POLEARMS);

        getOrCreateTagBuilder(UTags.Items.BASKETS).add(ItemFamilies.BASKETS);
        getOrCreateTagBuilder(UTags.Items.BADGES).add(Race.REGISTRY.stream()
                .map(race -> race.getId().withPath(p -> p + "_badge"))
                .flatMap(id -> Registries.ITEM.getOrEmpty(id).stream())
                .toArray(Item[]::new));
        getOrCreateTagBuilder(UTags.Items.WOOL_BED_SHEETS).add(BedsheetsItem.ITEMS.values().stream().filter(sheet -> sheet != UItems.KELP_BED_SHEETS).toArray(Item[]::new));
        getOrCreateTagBuilder(UTags.Items.BED_SHEETS).forceAddTag(UTags.Items.WOOL_BED_SHEETS).add(UItems.KELP_BED_SHEETS);
        getOrCreateTagBuilder(UTags.Items.APPLE_SEEDS).add(UItems.GREEN_APPLE_SEEDS, UItems.SWEET_APPLE_SEEDS, UItems.SOUR_APPLE_SEEDS);
        getOrCreateTagBuilder(UTags.Items.MAGIC_FEATHERS).add(UItems.PEGASUS_FEATHER, UItems.GRYPHON_FEATHER);
        getOrCreateTagBuilder(UTags.Items.FRESH_APPLES).add(Items.APPLE, UItems.GREEN_APPLE, UItems.SWEET_APPLE, UItems.SOUR_APPLE);
        getOrCreateTagBuilder(UTags.Items.CLOUD_JARS).add(UItems.RAIN_CLOUD_JAR, UItems.STORM_CLOUD_JAR);
        getOrCreateTagBuilder(UTags.Items.PIES).add(UItems.APPLE_PIE, UItems.APPLE_PIE_HOOF);

        // technical tags
        getOrCreateTagBuilder(ItemTags.VILLAGER_PLANTABLE_SEEDS).addTag(UTags.Items.APPLE_SEEDS);
        getOrCreateTagBuilder(UTags.Items.CAN_CUT_PIE).forceAddTag(ConventionalItemTags.SHEAR_TOOLS).addOptionalTag(UConventionalTags.Items.TOOL_KNIVES);
        getOrCreateTagBuilder(UTags.Items.COOLS_OFF_KIRINS).add(Items.MELON_SLICE, UItems.JUICE).forceAddTag(ConventionalItemTags.WATER_BUCKETS);
        getOrCreateTagBuilder(UTags.Items.FALLS_SLOWLY).add(Items.FEATHER, UItems.CLOUD_LUMP).forceAddTag(UTags.Items.MAGIC_FEATHERS);
        getOrCreateTagBuilder(UTags.Items.IS_DELIVERED_AGGRESSIVELY).forceAddTag(ItemTags.ANVIL);
        getOrCreateTagBuilder(UTags.Items.SPOOKED_MOB_DROPS).add(Items.BRICK);
        getOrCreateTagBuilder(UTags.Items.SHADES).add(
                Items.CARVED_PUMPKIN, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.PLAYER_HEAD,
                Items.ZOMBIE_HEAD, Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.PIGLIN_HEAD,
                UItems.SUNGLASSES
        );
        getOrCreateTagBuilder(UTags.Items.FLOATS_ON_CLOUDS)
            .forceAddTag(UTags.Items.CLOUD_BEDS)
            .forceAddTag(UTags.Items.CLOUD_SLABS)
            .forceAddTag(UTags.Items.CLOUD_STAIRS)
            .forceAddTag(UTags.Items.CLOUD_BLOCKS)
            .add(UItems.CLOUD_LUMP);
        getOrCreateTagBuilder(UTags.Items.CONTAINER_WITH_LOVE).add(UItems.LOVE_BOTTLE, UItems.LOVE_BUCKET, UItems.LOVE_MUG);
        getOrCreateTagBuilder(UTags.Items.HAS_NO_TRAITS).add(
                Items.AIR, Items.SPAWNER, Items.STRUCTURE_VOID, Items.STRUCTURE_BLOCK,
                Items.COMMAND_BLOCK, Items.CHAIN_COMMAND_BLOCK, Items.REPEATING_COMMAND_BLOCK,
                Items.LIGHT, Items.JIGSAW, Items.BARRIER, Items.BEDROCK, Items.END_PORTAL_FRAME,
                Items.DEBUG_STICK, Items.COMMAND_BLOCK_MINECART,
                UItems.PLUNDER_VINE
        ).forceAddTag(UTags.Items.BADGES);
        getOrCreateTagBuilder(UTags.Items.LOOT_BUG_COMMON_DROPS).forceAddTag(ConventionalItemTags.NUGGETS);
        getOrCreateTagBuilder(UTags.Items.LOOT_BUG_RARE_DROPS).add(
                    Items.DIAMOND, Items.GOLDEN_APPLE, Items.GOLDEN_CARROT,
                    Items.GOLDEN_HELMET, Items.GOLDEN_BOOTS, Items.GOLDEN_LEGGINGS, Items.GOLDEN_CHESTPLATE,
                    Items.GOLDEN_HORSE_ARMOR,
                    Items.GOLDEN_PICKAXE, Items.GOLDEN_SHOVEL, Items.GOLDEN_AXE, Items.GOLDEN_SWORD, Items.GOLDEN_HOE,
                    UItems.GOLDEN_HORSE_SHOE, UItems.GOLDEN_POLEARM, UItems.GOLDEN_FEATHER, UItems.GOLDEN_WING,
                    UItems.GOLDEN_OAK_SEEDS
            ).forceAddTag(ConventionalItemTags.GOLD_INGOTS).forceAddTag(ConventionalItemTags.GOLD_RAW_MATERIALS).forceAddTag(ConventionalItemTags.STORAGE_BLOCKS_RAW_GOLD)
            .addOptionalTag(Identifier.of("farmersdelight:golden_knife"));
        getOrCreateTagBuilder(UTags.Items.LOOT_BUG_EPIC_DROPS).add(
                Items.DIAMOND_BLOCK,
                Items.DIAMOND_HELMET, Items.DIAMOND_BOOTS, Items.DIAMOND_LEGGINGS, Items.DIAMOND_CHESTPLATE,
                Items.DIAMOND_HORSE_ARMOR,
                Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_AXE, Items.DIAMOND_SWORD, Items.DIAMOND_HOE,
                UItems.DIAMOND_POLEARM
        ).forceAddTag(UTags.Items.BADGES).forceAddTag(ConventionalItemTags.GOLD_INGOTS);
        getOrCreateTagBuilder(UTags.Items.SHELLS).add(Items.NAUTILUS_SHELL, UItems.CLAM_SHELL, UItems.SCALLOP_SHELL, UItems.TURRET_SHELL);
        getOrCreateTagBuilder(UTags.Items.SPECIAL_SHELLS).add(UItems.SHELLY);
        getOrCreateTagBuilder(UTags.Items.ROCK_STEWS).add(UItems.ROCK_STEW);
        getOrCreateTagBuilder(UTags.Items.HIGH_QUALITY_SEA_VEGETABLES)
            .add(Items.DRIED_KELP_BLOCK, Items.GLOW_LICHEN)
            .forceAddTag(UConventionalTags.Items.CORAL_BLOCKS);
        getOrCreateTagBuilder(UTags.Items.LOW_QUALITY_SEA_VEGETABLES)
            .add(Items.KELP, Items.DRIED_KELP, Items.SEAGRASS, Items.SEA_PICKLE)
            .forceAddTag(UConventionalTags.Items.CORALS).forceAddTag(UConventionalTags.Items.CORAL_FANS);

        exportForagingTags();
        exportCreativeTabs();
        exportFarmersDelightItems();
    }

    private void copyBlockTags() {
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.LOGS, ItemTags.LOGS);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.WOODEN_PRESSURE_PLATES, ItemTags.WOODEN_PRESSURE_PLATES);
        copy(BlockTags.SLABS, ItemTags.SLABS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.STAIRS, ItemTags.STAIRS);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.TRAPDOORS, ItemTags.TRAPDOORS);
        copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);

        copy(UTags.Blocks.ZAP_LOGS, UTags.Items.ZAP_LOGS);
        copy(UTags.Blocks.WAXED_ZAP_LOGS, UTags.Items.WAXED_ZAP_LOGS);
        copy(UTags.Blocks.PALM_LOGS, UTags.Items.PALM_LOGS);
        copy(UTags.Blocks.CLOUD_BEDS, UTags.Items.CLOUD_BEDS);
        copy(UTags.Blocks.CLOUD_SLABS, UTags.Items.CLOUD_SLABS);
        copy(UTags.Blocks.CLOUD_STAIRS, UTags.Items.CLOUD_STAIRS);
        copy(UTags.Blocks.CLOUD_BLOCKS, UTags.Items.CLOUD_BLOCKS);
        copy(UTags.Blocks.CHITIN_BLOCKS, UTags.Items.CHITIN_BLOCKS);

        copy(SereneSeasonsTags.Blocks.AUTUMN_CROPS, SereneSeasonsTags.Items.AUTUMN_CROPS);
        copy(SereneSeasonsTags.Blocks.WINTER_CROPS, SereneSeasonsTags.Items.WINTER_CROPS);
        copy(SereneSeasonsTags.Blocks.SPRING_CROPS, SereneSeasonsTags.Items.SPRING_CROPS);
        copy(SereneSeasonsTags.Blocks.SUMMER_CROPS, SereneSeasonsTags.Items.SUMMER_CROPS);
    }

    private void exportForagingTags() {
        getOrCreateTagBuilder(UTags.Items.FORAGE_BLINDING).add(Items.OXEYE_DAISY);
        getOrCreateTagBuilder(UTags.Items.FORAGE_DANGEROUS).add(Items.POPPY, Items.LILY_OF_THE_VALLEY);
        getOrCreateTagBuilder(UTags.Items.FORAGE_FILLING).add(Items.HAY_BLOCK);
        getOrCreateTagBuilder(UTags.Items.FORAGE_SAFE).add(Items.BLUE_ORCHID, Items.RED_TULIP, Items.ORANGE_TULIP,
                Items.PINK_TULIP, Items.CORNFLOWER, Items.PEONY, Items.SUNFLOWER, Items.DANDELION, Items.LILAC, Items.TALL_GRASS,
                Items.DEAD_BUSH, Items.PINK_PETALS
        ).forceAddTag(UConventionalTags.Items.MUSHROOMS).forceAddTag(ItemTags.SAPLINGS);
        getOrCreateTagBuilder(UTags.Items.FORAGE_NAUSEATING).add(Items.SHORT_GRASS, UItems.CIDER);
        getOrCreateTagBuilder(UTags.Items.FORAGE_PRICKLY).add(Items.ROSE_BUSH).forceAddTag(ItemTags.SAPLINGS);
        getOrCreateTagBuilder(UTags.Items.FORAGE_GLOWING).add(Items.AZURE_BLUET, Items.TORCHFLOWER);
        getOrCreateTagBuilder(UTags.Items.FORAGE_RISKY).add(Items.ALLIUM, Items.WHITE_TULIP, UItems.BURNED_JUICE);
        getOrCreateTagBuilder(UTags.Items.FORAGE_STRENGHENING).add(Items.FERN);
        getOrCreateTagBuilder(UTags.Items.FORAGE_SEVERE_NAUSEATING).add(Items.PITCHER_PLANT);
        getOrCreateTagBuilder(UTags.Items.FORAGE_SEVERE_PRICKLY).add(Items.LARGE_FERN);
        getOrCreateTagBuilder(UTags.Items.GROUP_FORAGING)
            .forceAddTag(UTags.Items.FORAGE_BLINDING)
            .forceAddTag(UTags.Items.FORAGE_DANGEROUS)
            .forceAddTag(UTags.Items.FORAGE_FILLING)
            .forceAddTag(UTags.Items.FORAGE_SAFE)
            .forceAddTag(UTags.Items.FORAGE_NAUSEATING)
            .forceAddTag(UTags.Items.FORAGE_PRICKLY)
            .forceAddTag(UTags.Items.FORAGE_GLOWING)
            .forceAddTag(UTags.Items.FORAGE_RISKY)
            .forceAddTag(UTags.Items.FORAGE_STRENGHENING)
            .forceAddTag(UTags.Items.FORAGE_SEVERE_NAUSEATING)
            .forceAddTag(UTags.Items.FORAGE_SEVERE_PRICKLY);
    }

    private void exportCreativeTabs() {
        getOrCreateTagBuilder(UTags.Items.GROUP_UNICORN).add(
                UItems.SPELLBOOK, UItems.GEMSTONE, UItems.BOTCHED_GEM, UItems.FRIENDSHIP_BRACELET,
                UItems.CRYSTAL_HEART, UItems.CRYSTAL_SHARD, UBlocks.CRYSTAL_DOOR.asItem(),
                UItems.MEADOWBROOKS_STAFF, UItems.MAGIC_STAFF, UItems.GROGARS_BELL,
                UItems.DRAGON_BREATH_SCROLL, UItems.PEGASUS_AMULET, UItems.ALICORN_AMULET,
                UItems.BROKEN_ALICORN_AMULET, UItems.UNICORN_AMULET, UItems.SPECTRAL_CLOCK
        );
        getOrCreateTagBuilder(UTags.Items.GROUP_PEGASUS)
            .add(UBlocks.SHAPING_BENCH.asItem(), UBlocks.CLOUD_CHEST.asItem(), UItems.CLOUD_LUMP)
            .add(List.of(UBlockFamilies.CLOUD, UBlockFamilies.CLOUD_PLANKS, UBlockFamilies.CLOUD_BRICKS, UBlockFamilies.DENSE_CLOUD, UBlockFamilies.ETCHED_CLOUD)
                    .stream()
                    .map(BlockFamily::getVariants)
                    .map(Map::values)
                    .flatMap(Collection::stream)
                    .map(ItemConvertible::asItem)
                    .filter(Objects::nonNull)
                    .toArray(Item[]::new))
            .add(UBlocks.UNSTABLE_CLOUD.asItem(), UBlocks.CLOUD_PILLAR.asItem(), UBlocks.CLOUD_DOOR.asItem(), UBlocks.CLOUD_BED.asItem())
            .forceAddTag(UTags.Items.BED_SHEETS)
            .forceAddTag(ConventionalItemTags.RAW_FISH_FOODS)
            .forceAddTag(ConventionalItemTags.COOKED_FISH_FOODS)
            .forceAddTag(UConventionalTags.Items.ROTTEN_FISH)
            .forceAddTag(UTags.Items.CLOUD_JARS)
            .add(UItems.LIGHTNING_JAR)
            .forceAddTag(UTags.Items.POLEARMS)
            .add(UItems.PEGASUS_FEATHER, UItems.GRYPHON_FEATHER, UItems.GOLDEN_FEATHER, UItems.GOLDEN_WING);
        getOrCreateTagBuilder(UTags.Items.GROUP_EARTH_PONY)
            .add(
                UBlocks.GREEN_APPLE_LEAVES.asItem(), UBlocks.SWEET_APPLE_LEAVES.asItem(), UBlocks.SOUR_APPLE_LEAVES.asItem(), UBlocks.ZAP_LEAVES.asItem(), UBlocks.FLOWERING_ZAP_LEAVES.asItem(),
                UBlocks.ZAP_LOG.asItem(), UBlocks.ZAP_WOOD.asItem(),
                UBlocks.STRIPPED_ZAP_LOG.asItem(), UBlocks.STRIPPED_ZAP_WOOD.asItem(),
                UBlocks.ZAP_PLANKS.asItem(), UBlocks.ZAP_STAIRS.asItem(), UBlocks.ZAP_SLAB.asItem(), UBlocks.ZAP_FENCE.asItem(), UBlocks.ZAP_FENCE_GATE.asItem(),

                UBlocks.WAXED_ZAP_LOG.asItem(), UBlocks.WAXED_ZAP_WOOD.asItem(),
                UBlocks.WAXED_STRIPPED_ZAP_LOG.asItem(), UBlocks.WAXED_STRIPPED_ZAP_WOOD.asItem(),
                UBlocks.WAXED_ZAP_PLANKS.asItem(), UBlocks.WAXED_ZAP_STAIRS.asItem(), UBlocks.WAXED_ZAP_SLAB.asItem(), UBlocks.WAXED_ZAP_FENCE.asItem(), UBlocks.WAXED_ZAP_FENCE_GATE.asItem(),

                UItems.CANDIED_APPLE, UBlocks.STABLE_DOOR.asItem(), UBlocks.DARK_OAK_DOOR.asItem())
            .forceAddTag(UTags.Items.FRESH_APPLES)
            .add(
                UItems.ZAP_APPLE, UItems.ZAP_BULB, UItems.ROTTEN_APPLE,
                UItems.GREEN_APPLE_SEEDS, UItems.SWEET_APPLE_SEEDS, UItems.SOUR_APPLE_SEEDS,
                UTreeGen.ZAP_APPLE_TREE.sapling().get().asItem(),
                UTreeGen.BANANA_TREE.sapling().get().asItem(),
                UItems.CURING_JOKE, UItems.MANGO, UItems.EMPTY_JAR, UItems.COOKED_ZAP_APPLE,
                UItems.ZAP_APPLE_JAM_JAR,
                UItems.OAT_SEEDS, UItems.OATS, UItems.IMPORTED_OATS,
                UItems.OATMEAL, UItems.OATMEAL_COOKIE, UItems.CHOCOLATE_OATMEAL_COOKIE,
                UItems.DAFFODIL_DAISY_SANDWICH, UItems.HAY_BURGER, UItems.HAY_BURGER, UItems.HAY_FRIES,
                UItems.CRISPY_HAY_FRIES, UItems.HORSE_SHOE_FRIES, UItems.WHEAT_WORMS,
                UItems.BAITED_FISHING_ROD, UBlocks.WORM_BLOCK.asItem(),
                UItems.MUFFIN, UItems.SCONE, UItems.ACORN, UItems.PINECONE, UItems.PINECONE_COOKIE,
                UItems.BOWL_OF_NUTS, UItems.CRYSTAL_SHARD, UItems.PEBBLES, UItems.ROCK, UItems.WEIRD_ROCK,
                UItems.ROCK_STEW, UItems.ROCK_CANDY, UItems.SALT_CUBE, UItems.MUG, UItems.CIDER, UItems.JUICE,
                UItems.BURNED_JUICE, UItems.TOAST, UItems.JAM_TOAST, UItems.BURNED_TOAST, UItems.APPLE_PIE,
                UItems.APPLE_PIE_HOOF, UItems.APPLE_PIE_SLICE, UBlocks.WEATHER_VANE.asItem()
            )
            .forceAddTag(UTags.Items.BASKETS)
            .add(UItems.GIANT_BALLOON, UBlocks.CLOTH_BED.asItem())
            .forceAddTag(UTags.Items.BED_SHEETS);
        getOrCreateTagBuilder(UTags.Items.GROUP_BAT_PONY)
            .forceAddTag(UConventionalTags.Items.RAW_INSECT)
            .forceAddTag(UConventionalTags.Items.COOKED_INSECT)
            .forceAddTag(UTags.Items.POLEARMS)
            .add(
                UBlocks.MANGO_LEAVES.asItem(), UTreeGen.MANGO_TREE.sapling().get().asItem(), UItems.MANGO,
                UItems.PINEAPPLE, UItems.PINEAPPLE_CROWN, UItems.BANANA, UItems.SUNGLASSES, UItems.BROKEN_SUNGLASSES
            );
        getOrCreateTagBuilder(UTags.Items.GROUP_CHANGELING)
            .add(
                UItems.CARAPACE, UBlocks.SURFACE_CHITIN.asItem(), UBlocks.CHITIN.asItem()
            ).add(UBlockFamilies.CHISELED_CHITIN.getVariants().values().stream().map(ItemConvertible::asItem).toArray(Item[]::new))
            .add(
                UBlocks.CHISELLED_CHITIN_HULL.asItem(), UBlocks.CHITIN_SPIKES.asItem(),
                UBlocks.SLIME_PUSTULE.asItem(),
                UBlocks.MYSTERIOUS_EGG.asItem(), UItems.GREEN_FRIED_EGG,
                UBlocks.HIVE.asItem()
            )
            .forceAddTag(ConventionalItemTags.RAW_MEAT_FOODS)
            .forceAddTag(ConventionalItemTags.COOKED_MEAT_FOODS)
            .forceAddTag(UConventionalTags.Items.ROTTEN_MEAT)
            .forceAddTag(UConventionalTags.Items.RAW_INSECT)
            .forceAddTag(UConventionalTags.Items.COOKED_INSECT)
            .forceAddTag(UConventionalTags.Items.ROTTEN_INSECT)
            .forceAddTag(UTags.Items.CONTAINER_WITH_LOVE);
        getOrCreateTagBuilder(UTags.Items.GROUP_SEA_PONY)
            .add(UItems.PEARL_NECKLACE)
            .forceAddTag(UTags.Items.SHELLS)
            .forceAddTag(UTags.Items.SPECIAL_SHELLS)
            .forceAddTag(UTags.Items.LOW_QUALITY_SEA_VEGETABLES)
            .forceAddTag(UTags.Items.HIGH_QUALITY_SEA_VEGETABLES);
    }

    private void exportConventionalTags() {
        copy(ConventionalBlockTags.CONCRETES, UConventionalTags.Items.CONCRETES);
        copy(UConventionalTags.Blocks.CORAL_BLOCKS, UConventionalTags.Items.CORAL_BLOCKS);
        copy(UConventionalTags.Blocks.CORAL_FANS, UConventionalTags.Items.CORAL_FANS);
        copy(UConventionalTags.Blocks.CORALS, UConventionalTags.Items.CORALS);
        getOrCreateTagBuilder(UConventionalTags.Items.ACORNS).add(UItems.ACORN);
        getOrCreateTagBuilder(UConventionalTags.Items.APPLES)
            .add(Items.APPLE, Items.GOLDEN_APPLE, Items.ENCHANTED_GOLDEN_APPLE, UItems.ROTTEN_APPLE)
            .forceAddTag(UTags.Items.FRESH_APPLES)
            .addOptionalTag(Identifier.of("c", "pyrite_apples")) // no idea which mod add pyrite apples
        ;
        getOrCreateTagBuilder(UConventionalTags.Items.BANANAS).add(UItems.BANANA);
        getOrCreateTagBuilder(ConventionalItemTags.RAW_FISH_FOODS).addOptionalTag(Identifier.of("c", "mollusks"));
        getOrCreateTagBuilder(ConventionalItemTags.COOKED_FISH_FOODS).add(UItems.COOKED_TROPICAL_FISH, UItems.COOKED_PUFFERFISH, UItems.FRIED_AXOLOTL);
        getOrCreateTagBuilder(UConventionalTags.Items.ROTTEN_FISH).add(UItems.ROTTEN_COD, UItems.ROTTEN_TROPICAL_FISH, UItems.ROTTEN_SALMON, UItems.ROTTEN_PUFFERFISH);
        getOrCreateTagBuilder(ItemTags.FISHES).add(
                UItems.COOKED_TROPICAL_FISH, UItems.COOKED_PUFFERFISH, UItems.FRIED_AXOLOTL,
                UItems.ROTTEN_COD, UItems.ROTTEN_TROPICAL_FISH, UItems.ROTTEN_SALMON, UItems.ROTTEN_PUFFERFISH
        );
        getOrCreateTagBuilder(ConventionalItemTags.COOKED_MEAT_FOODS)
            .addOptionalTag(Identifier.of("c", "cooked_bacon"))
            .addOptionalTag(Identifier.of("c", "cooked_beef"))
            .addOptionalTag(Identifier.of("c", "cooked_chicken"))
            .addOptionalTag(Identifier.of("c", "cooked_mutton"))
            .addOptionalTag(Identifier.of("c", "cooked_pork"))
            .addOptionalTag(Identifier.of("c", "fried_chickens"))
            .addOptionalTag(Identifier.of("c", "hamburgers"))
            .addOptionalTag(Identifier.of("c", "pork_and_beans"))
            .addOptionalTag(Identifier.of("c", "pork_jerkies"))
            .addOptionalTag(Identifier.of("c", "protien"));
        getOrCreateTagBuilder(ConventionalItemTags.RAW_MEAT_FOODS)
            .addOptionalTag(Identifier.of("c", "raw_bacon"))
            .addOptionalTag(Identifier.of("c", "raw_beef"))
            .addOptionalTag(Identifier.of("c", "raw_chicken"))
            .addOptionalTag(Identifier.of("c", "raw_mutton"))
            .addOptionalTag(Identifier.of("c", "raw_pork"))
            .addOptionalTag(Identifier.of("c", "lemon_chickens"));
        getOrCreateTagBuilder(UConventionalTags.Items.ROTTEN_MEAT).add(Items.ROTTEN_FLESH);
        getOrCreateTagBuilder(UConventionalTags.Items.ROTTEN_INSECT).add(Items.FERMENTED_SPIDER_EYE);
        getOrCreateTagBuilder(UConventionalTags.Items.COOKED_INSECT).add(UItems.COOKED_FROG_LEGS);
        getOrCreateTagBuilder(UConventionalTags.Items.RAW_INSECT).add(Items.SPIDER_EYE, UItems.BUTTERFLY, UItems.FROG_LEGS, UItems.WHEAT_WORMS, UBlocks.WORM_BLOCK.asItem());
        getOrCreateTagBuilder(UConventionalTags.Items.WORMS).add(UItems.WHEAT_WORMS);
        getOrCreateTagBuilder(UConventionalTags.Items.ROCKS).add(UItems.ROCK);
        getOrCreateTagBuilder(UConventionalTags.Items.GEMS).add(UItems.GEMSTONE, UItems.BOTCHED_GEM);
        getOrCreateTagBuilder(UConventionalTags.Items.PINECONES).add(UItems.PINECONE);
        getOrCreateTagBuilder(UConventionalTags.Items.PINEAPPLES).add(UItems.PINEAPPLE);
        getOrCreateTagBuilder(UConventionalTags.Items.MANGOES).add(UItems.MANGO);
        getOrCreateTagBuilder(UConventionalTags.Items.MUSHROOMS).add(Items.RED_MUSHROOM, Items.BROWN_MUSHROOM);
        getOrCreateTagBuilder(UConventionalTags.Items.MUFFINS).add(UItems.MUFFIN);
        getOrCreateTagBuilder(UConventionalTags.Items.SEEDS).add(Items.BEETROOT_SEEDS, Items.MELON_SEEDS, Items.PUMPKIN_SEEDS, Items.TORCHFLOWER_SEEDS, Items.WHEAT_SEEDS)
            .add(UItems.OAT_SEEDS)
            .forceAddTag(UTags.Items.APPLE_SEEDS);
        getOrCreateTagBuilder(UConventionalTags.Items.COOKIES).add(Items.COOKIE, UItems.OATMEAL_COOKIE, UItems.CHOCOLATE_OATMEAL_COOKIE, UItems.PINECONE_COOKIE);
        getOrCreateTagBuilder(UConventionalTags.Items.OATMEALS).add(UItems.OATMEAL);
        getOrCreateTagBuilder(UConventionalTags.Items.GRAIN).add(Items.WHEAT, UItems.OATS);
        getOrCreateTagBuilder(UConventionalTags.Items.NUTS).add(UItems.BOWL_OF_NUTS)
            .addOptionalTag(UConventionalTags.Items.CROPS_PEANUTS)
            .forceAddTag(UConventionalTags.Items.ACORNS)
            .addOptional(Identifier.of("garnished", "nuts"))
            .addOptional(Identifier.of("garnished", "nut_mix"))
            .addOptional(Identifier.of("garnished", "neverable_delecacies"));
        getOrCreateTagBuilder(ConventionalItemTags.FRUIT_FOODS)
            .add(Items.MELON_SLICE, Items.SWEET_BERRIES, Items.GLOW_BERRIES, Items.CHORUS_FRUIT)
            .add(UItems.JUICE, UItems.ZAP_APPLE, UItems.ZAP_BULB)
            .forceAddTag(UConventionalTags.Items.MANGOES)
            .forceAddTag(UConventionalTags.Items.PINEAPPLES)
            .forceAddTag(UConventionalTags.Items.APPLES)
            .forceAddTag(UConventionalTags.Items.BANANAS)
            .addOptionalTag(Identifier.of("garnished", "berries"));
        getOrCreateTagBuilder(UConventionalTags.Items.DESSERTS).add(Items.CAKE, UItems.APPLE_PIE_SLICE).forceAddTag(UTags.Items.PIES);
        getOrCreateTagBuilder(ConventionalItemTags.CANDY_FOODS).add(Items.SUGAR, UItems.ROCK_CANDY, UItems.CANDIED_APPLE);
        getOrCreateTagBuilder(UTags.Items.BAKED_GOODS).add(
                Items.BREAD, Items.COOKIE, Items.PUMPKIN_PIE,
                UItems.MUFFIN, UItems.SCONE, UItems.COOKED_ZAP_APPLE, UItems.TOAST, UItems.BURNED_TOAST, UItems.JAM_TOAST, UItems.IMPORTED_OATS,
                UItems.HAY_FRIES, UItems.CRISPY_HAY_FRIES, UItems.HORSE_SHOE_FRIES)
            .forceAddTag(UConventionalTags.Items.OATMEALS)
            .forceAddTag(UConventionalTags.Items.COOKIES);
    }

    private void exportFarmersDelightItems() {
        getOrCreateTagBuilder(UTags.Items.COOLS_OFF_KIRINS)
            .addOptional(Identifier.of("farmersdelight", "melon_popsicle"))
            .addOptional(Identifier.of("farmersdelight", "melon_juice"));
        getOrCreateTagBuilder(TagKey.of(RegistryKeys.ITEM, Identifier.of("farmersdelight", "cabbage_roll_ingredients"))).add(UItems.OATS, UItems.ROCK, UItems.WHEAT_WORMS);
        getOrCreateTagBuilder(TagKey.of(RegistryKeys.ITEM, Identifier.of("farmersdelight", "comfort_foods"))).add(UItems.OATMEAL, UItems.ROCK_STEW, UItems.MUFFIN);
        getOrCreateTagBuilder(ConventionalItemTags.RAW_FISH_FOODS)
            .addOptional(Identifier.of("farmersdelight", "cod_roll"))
            .addOptional(Identifier.of("farmersdelight", "salmon_roll"))
            .addOptional(Identifier.of("farmersdelight", "cod_slice"))
            .addOptional(Identifier.of("farmersdelight", "salmon_slice"));
        getOrCreateTagBuilder(ConventionalItemTags.COOKED_FISH_FOODS)
            .addOptional(Identifier.of("farmersdelight", "fish_stew"))
            .addOptional(Identifier.of("farmersdelight", "baked_cod_stew"))
            .addOptional(Identifier.of("farmersdelight", "grilled_salmon"));
        getOrCreateTagBuilder(ConventionalItemTags.RAW_MEAT_FOODS)
            .addOptional(Identifier.of("farmersdelight", "ham"));
        getOrCreateTagBuilder(ConventionalItemTags.COOKED_MEAT_FOODS)
            .addOptional(Identifier.of("farmersdelight", "chicken_soup"))
            .addOptional(Identifier.of("farmersdelight", "bacon_and_eggs"))
            .addOptional(Identifier.of("farmersdelight", "pasta_with_meatballs"))
            .addOptional(Identifier.of("farmersdelight", "beef_stew"))
            .addOptional(Identifier.of("farmersdelight", "bone_broth"))
            .addOptional(Identifier.of("farmersdelight", "mutton_wrap"))
            .addOptional(Identifier.of("farmersdelight", "bacon_sandwich"))
            .addOptional(Identifier.of("farmersdelight", "hamburger"))
            .addOptional(Identifier.of("farmersdelight", "chicken_sandwich"))
            .addOptional(Identifier.of("farmersdelight", "barbecue_stick"))
            .addOptional(Identifier.of("farmersdelight", "smoked_ham"))
            .addOptional(Identifier.of("farmersdelight", "honey_glazed_ham"))
            .addOptional(Identifier.of("farmersdelight", "honey_glazed_ham_block"))
            .addOptional(Identifier.of("farmersdelight", "roast_chicken"))
            .addOptional(Identifier.of("farmersdelight", "roast_chicken_block"))
            .addOptional(Identifier.of("farmersdelight", "steak_and_potatoes"))
            .addOptional(Identifier.of("farmersdelight", "roasted_mutton_chops"))
            .addOptional(Identifier.of("farmersdelight", "pasta_with_mutton_chop"));
        getOrCreateTagBuilder(ConventionalItemTags.FRUIT_FOODS)
            .addOptional(Identifier.of("farmersdelight", "pumpkin_slice"))
            .addOptional(Identifier.of("farmersdelight", "tomato"))
            .addOptional(Identifier.of("farmersdelight", "melon_juice"))
            .addOptional(Identifier.of("farmersdelight", "fruit_salad"));
        getOrCreateTagBuilder(UConventionalTags.Items.DESSERTS)
            .addOptional(Identifier.of("farmersdelight", "sweet_berry_cheesecake"))
            .addOptional(Identifier.of("farmersdelight", "sweet_berry_cheesecake_slice"))
            .addOptional(Identifier.of("farmersdelight", "chocolate_pie_slice"))
            .addOptional(Identifier.of("farmersdelight", "cake_slice"))
            .addOptional(Identifier.of("farmersdelight", "apple_pie_slice"))
            .addOptional(Identifier.of("farmersdelight", "glow_berry_custard"));
        getOrCreateTagBuilder(UConventionalTags.Items.COOKIES)
            .addOptional(Identifier.of("farmersdelight", "sweet_berry_cookie"))
            .addOptional(Identifier.of("farmersdelight", "honey_cookie"));
        getOrCreateTagBuilder(UTags.Items.BAKED_GOODS)
            .addOptional(Identifier.of("farmersdelight", "wheat_dough"))
            .addOptional(Identifier.of("farmersdelight", "raw_pasta"))
            .addOptional(Identifier.of("farmersdelight", "pie_crust"))
            .addOptional(Identifier.of("farmersdelight", "egg_sandwich"));
        getOrCreateTagBuilder(UTags.Items.HIGH_QUALITY_SEA_VEGETABLES)
            .addOptional(Identifier.of("farmersdelight", "kelp_roll"));
        getOrCreateTagBuilder(UTags.Items.LOW_QUALITY_SEA_VEGETABLES)
            .addOptional(Identifier.of("farmersdelight", "kelp_roll_slice"));
        getOrCreateTagBuilder(UTags.Items.FORAGE_FILLING)
            .addOptional(Identifier.of("farmersdelight", "horse_feed"))
            .addOptional(Identifier.of("farmersdelight", "rice_bale"))
            .addOptional(Identifier.of("farmersdelight", "straw_bale"));
        getOrCreateTagBuilder(UTags.Items.FORAGE_SAFE)
            .addOptional(Identifier.of("farmersdelight", "sandy_shrub"))
            .addOptional(Identifier.of("farmersdelight", "wild_cabbages"))
            .addOptional(Identifier.of("farmersdelight", "wild_onions"))
            .addOptional(Identifier.of("farmersdelight", "wild_carrots"))
            .addOptional(Identifier.of("farmersdelight", "wild_beetroots"))
            .addOptional(Identifier.of("farmersdelight", "wild_rice"));
        getOrCreateTagBuilder(UTags.Items.FORAGE_RISKY)
            .addOptional(Identifier.of("farmersdelight", "wild_tomatoes"))
            .addOptional(Identifier.of("farmersdelight", "wild_potatoes"))
            .addOptionalTag(Identifier.of("c", "meads"));
    }
}
